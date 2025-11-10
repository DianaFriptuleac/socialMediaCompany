package dianafriptuleac.socialMediaCompany.tools;

import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.exceptions.UnauthorizedException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
// Indica a Spring che questa classe è un "componente" gestito dal container.
// Spring crea automaticamente un’istanza di questa classe
// e permette di iniettarla (con @Autowired) dove serve, ad esempio in AuthService

public class JWT {
    @Value("${jwt.secret}")
    // Recupera automaticamente il valore della proprietà `jwt.secret`  dal file application.properties
    // Il segreto usato per firmare e verificare i token

    private String secret;

    // CREA UN TOKEN JWT
    public String createToken(User user) {
        return Jwts.builder()   // Crea un nuovo builder per costruire il token JWT
                .issuedAt(new Date(System.currentTimeMillis()))  //data di emissione del token (momento attuale)
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7))  // la scadenza: in questo caso 7 giorni dopo l’emissione
                .subject(String.valueOf(user.getId())) //“subject” del token - l’ID dell’utente (usato per riconoscerlo)
                .claim("role", user.getRole())  //Aggiunge un “claim” al token - ruolo dell’utente (USER, ADMIN)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                // Firma il token con l’algoritmo HMAC-SHA usando la chiave segreta convertita in byte.
                // È ciò che garantisce che il token non possa essere modificato da altri

                .compact();
        // Converte il token in una stringa compatta (codificata in Base64) pronta per essere inviata al client
    }


    // VERIFICA TOKEN JWT
    public void verifyToken(String accessToken) {
        try {
            Jwts.parser()  // Crea un parser per analizzare (verificare) il token JWT
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                    // Imposta la chiave segreta con cui è stato firmato il token:
                    // se il token è stato modificato, la verifica fallisce

                    .build().parse(accessToken);
            //Esegue effettivamente la verifica: controlla la firma e la validità (es. scadenza)

        } catch (Exception ex) {
            throw new UnauthorizedException("Token problems! Please log in again!");
        }
    }


    // ESTRAE L’ID DELL’UTENTE DAL TOKEN
    public String getIdFromToken(String accessToken) {
        return Jwts.parser()      //Crea un parser per leggere il token
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))    //Verifica la firma per assicurarsi che il token sia valido

                .build()
                .parseSignedClaims(accessToken) //Analizza il token JWT e ne estrae i “claims” (dati contenuti all’interno)

                .getPayload().getSubject();  //Recupera dal payload il “subject” del token, che è l’ID utente
    }

}
