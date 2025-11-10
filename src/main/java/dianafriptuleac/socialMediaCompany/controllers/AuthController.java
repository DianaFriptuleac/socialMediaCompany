package dianafriptuleac.socialMediaCompany.controllers;

import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.exceptions.BadRequestException;
import dianafriptuleac.socialMediaCompany.payloads.UserDTO;
import dianafriptuleac.socialMediaCompany.payloads.UserLoginDTO;
import dianafriptuleac.socialMediaCompany.payloads.UserLoginResponseDTO;
import dianafriptuleac.socialMediaCompany.services.AuthService;
import dianafriptuleac.socialMediaCompany.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
// Controller REST di Spring → Tutti i metodi restituiranno oggetti JSON nelle risposte HTTP

@RequestMapping("/auth")  // "percorso base" per tutte le rotte di questo controller

public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;


    // REGISTRAZIONE UTENTE
    @PostMapping("/register")   // auth/register
    @ResponseStatus(HttpStatus.CREATED)

    public User register(@RequestBody @Validated UserDTO body, BindingResult validationResult) {
        // @RequestBody → dice a Spring di leggere il corpo della richiesta JSON e convertirlo in un oggetto UserDTO
        // @Validated → attiva la validazione (basata sulle annotazioni in UserDTO)
        // BindingResult → contiene gli eventuali errori di validazione

        if (validationResult.hasErrors()) {
            String message = validationResult.getAllErrors().stream().map(objectError -> objectError.getDefaultMessage())
                    .collect(Collectors.joining(". "));
            // Raccoglie tutti i messaggi di errore e li unisce in una stringa leggibile
            throw new BadRequestException("Payload errors " + message);
            // Lancia un’eccezione personalizzata che restituirà un errore HTTP 400 (Bad Request)
        }
        return this.userService.save(body);  // salva utente nel DB
    }

    // LOGIN UTENTE
    @PostMapping("/login")   // /auth/login
    public UserLoginResponseDTO login(@RequestBody UserLoginDTO body) {
        // Legge le credenziali (email e password) dal corpo della richiesta
        // Delega ad AuthService la verifica delle credenziali e la generazione del token JWT

        return this.authService.checkAllCredentialsAndToken(body);
        // Restituisce un oggetto `UserLoginResponseDTO` contenente: il token JWT e
        // i dati essenziali dell’utente (id, nome, cognome, email, avatar)
    }
}
