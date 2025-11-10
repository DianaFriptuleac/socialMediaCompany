package dianafriptuleac.socialMediaCompany.security;

import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.services.UserService;
import dianafriptuleac.socialMediaCompany.tools.JWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

// Intercetta ogni richiesta HTTP e verificare
// se il token JWT è valido prima di permettere l’accesso alle risorse protette

@Component      // questa classe è un componente gestito dal container
public class JWTCheckerFilter extends OncePerRequestFilter {
    // OncePerRequestFilter → classe base per definire un filtro HTTP personalizzato
    // Viene eseguito prima che il controller gestisca la richiesta per controllare token o sessioni

    @Autowired
    private JWT jwt;
    //componente che gestisce i token JWT (creazione, verifica, estrazione dati)

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getServletPath();   //Ottiene il percorso (endpoint) della richiesta, es: "/api/posts".
        String authHeader = request.getHeader("Authorization");
        //Recupera l’intestazione "Authorization" della richiesta,dove viene inviato il token JWT con formato: "Bearer <token>".


        // Salta il controllo per endpoint pubblici
        if (shouldNotFilter(request)) {
            //Se l’endpoint è pubblico (es. /auth/login), non controlla il token
            filterChain.doFilter(request, response);
            return;
        }
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            //Se l’header è mancante o non comincia con "Bearer ", non c’è un token valido.
            // Lascia passare la richiesta (potrà essere rifiutata dopo, se serve).
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = authHeader.substring(7);  // Estrae il token JWT rimuovendo la parte "Bearer "
        try {
            jwt.verifyToken(accessToken);
            // Verifica che il token sia valido e non scaduto.
            // Se è invalido, genera un’eccezione e va nel blocco catch.

            String utenteId = jwt.getIdFromToken(accessToken);   // Estrae dal token l’ID dell’utente
            User currentUser = this.userService.findById(UUID.fromString(utenteId));  // Recupera dal database l’utente corrispondente a quell’ID

            // Crea un oggetto Authentication contenente l’utente e i suoi permessi (ruoli)
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    currentUser, null, currentUser.getAuthorities());


            // Imposta nel contesto di sicurezza di Spring l’utente autenticato
            // Da qui in poi, la richiesta è considerata “autenticata”
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Passa la richiesta al prossimo filtro della catena (o al controller, se è l’ultimo)
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            // restituisci 401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");  // che la risposta in formato JSON
            response.getWriter().write("{\"message\":\"Invalid or missing token\"}");  // messaggio JSON
        }
    }

    @Override
    //Metodo di supporto che decide se un certo endpoint va escluso dal controllo JWT
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();  // il percorso della richiesta

        // Rotte pubbliche / health / error / statiche
        return path.startsWith("/auth/")
                || path.equals("/api/ricetteEsterne/allRicette")
                || path.equals("/")
                || path.startsWith("/health")
                || path.startsWith("/actuator")
                || path.startsWith("/error")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");

        // true per tutti gli endpoint pubblici o tecnici, quindi il filtro non controlla il token JWT
    }
}

