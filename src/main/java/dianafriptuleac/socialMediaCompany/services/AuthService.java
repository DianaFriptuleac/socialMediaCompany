package dianafriptuleac.socialMediaCompany.services;

import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.exceptions.UnauthorizedException;
import dianafriptuleac.socialMediaCompany.payloads.UserLoginDTO;
import dianafriptuleac.socialMediaCompany.payloads.UserLoginResponseDTO;
import dianafriptuleac.socialMediaCompany.tools.JWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service  // componente della logica di business
public class AuthService {
    @Autowired
    private UserService userService;   // serve per cercare l’utente tramite email

    @Autowired
    private JWT jwt;
    // token JWT: - creazione (createToken) - validazione - estrazione dati (id, role, ecc.)

    @Autowired
    private PasswordEncoder bcrypt;
    // PasswordEncoder configurato (BCrypt, definito in SecurityConfig)
    // per confrontare la password in chiaro con quella hashata nel database

    public UserLoginResponseDTO checkAllCredentialsAndToken(UserLoginDTO body) {
        //  Metodo principale che:
        //  1️. Controlla le credenziali (email + password)
        //  2️. Se corrette → genera un token JWT
        //  3️. Restituisce i dati dell’utente + token

        User userFound = this.userService.findByEmail(body.email());
        // Cerca nel DB un utente con l’email passata nel DTO (body.email())
        // Se non esiste - lancia un’eccezione

        if (bcrypt.matches(body.password(), userFound.getPassword())) {
            //  Controlla che la password inviata corrisponda all’hash salvato nel DB:
            //  - body.password() → password in chiaro inviata nel login
            //  - userFound.getPassword() → password hashata nel database
            //  Se coincidono, l’utente è autenticato

            String accessToken = jwt.createToken(userFound);
            // Crea un token JWT contenente l’ID e il ruolo dell’utente che servirà per accedere alle rotte protette

            return new UserLoginResponseDTO(
                    accessToken,                   // token JWR generato
                    userFound.getId(),             // ID utente
                    userFound.getName(),
                    userFound.getSurname(),
                    userFound.getEmail(),
                    userFound.getAvatar()
            );
            // Restituisce un DTO (Data Transfer Object) con tutti i dati necessari
            // per il frontend dopo il login (incluso il token)
        } else {
            throw new UnauthorizedException("Incorrect user credentials.");
        }
    }
}
