package dianafriptuleac.socialMediaCompany.services;

import dianafriptuleac.socialMediaCompany.entities.PasswordResetToken;
import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.exceptions.UnauthorizedException;
import dianafriptuleac.socialMediaCompany.payloads.ForgotPasswordDTO;
import dianafriptuleac.socialMediaCompany.payloads.ResetPasswordDTO;
import dianafriptuleac.socialMediaCompany.payloads.UserLoginDTO;
import dianafriptuleac.socialMediaCompany.payloads.UserLoginResponseDTO;
import dianafriptuleac.socialMediaCompany.repositories.PasswordResetTokenRepository;
import dianafriptuleac.socialMediaCompany.tools.JWT;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


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

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private EmailSevice emailSevice;

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

            String accessToken = jwt.createToken(userFound, Boolean.TRUE.equals(body.rememberMe()));
            // Crea un token JWT contenente l’ID e il ruolo dell’utente che servirà per accedere alle rotte protette

            return new UserLoginResponseDTO(
                    accessToken,                   // token JWR generato
                    userFound.getId(),             // ID utente
                    userFound.getName(),
                    userFound.getSurname(),
                    userFound.getEmail(),
                    userFound.getAvatar(),
                    userFound.getRole()
            );
            // Restituisce un DTO (Data Transfer Object) con tutti i dati necessari
            // per il frontend dopo il login (incluso il token)
        } else {
            throw new UnauthorizedException("Incorrect user credentials.");
        }
    }

    @Transactional
    public void forgotPassword(ForgotPasswordDTO body) {
        // Cerca utente tramite email
        Optional<User> userOptional = userService.findOptionalByEmail(body.email());
        if (userOptional.isEmpty()) {
            return;
        }

        User user = userOptional.get();  // get user

        // Cancella eventuali token vecchi già creati per questo utente
        // Così rimane valido solo l'ultimo link di reset password

        // Cerca un eventuale token già associato all'utente
        // Se esiste lo aggiorneremo
        // Se non esiste ne verrà creato uno nuovo
        PasswordResetToken resetToken = passwordResetTokenRepository.findByUser(user)
                .orElseGet(PasswordResetToken::new);

        // Crea un token casuale unico
        // Sarà il codice segreto dentro il link di reset
        String token = UUID.randomUUID().toString();

        resetToken.setToken(token);

        // Collego il token all'utente che ha chiesto il reset password
        resetToken.setUser(user);

        // Imposta la scadenza del token tra 30 minuti
        // Dopo 30 minuti il link non sarà più valido
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        // Salva token nel DB
        passwordResetTokenRepository.save(resetToken);

        // Creo il link che l'utente riceverà via email
        // Il frontend leggerà il token dalla query string:
        // /reset-password?token=...
        String resetLink = "http://localhost:5173/reset_password?token=" + token;

        /// !!!!!!!!da modificare con https al deploy

        // Manda la mail all'utente con il link per cambiare password
        emailSevice.sendPasswordResetEmail(user.getEmail(), resetLink);

    }

    @Transactional
    public void resetPassword(ResetPasswordDTO body) {

        // Cerca nella tabella password_reset il token ricevuto dal frontend
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(body.token())
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        // Controlla se il token è scaduto
        // Se expiresAt è prima dell'orario attuale, significa che è scaduto
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {

            // Cancella il token scaduto dal database e blocca il reset password
            passwordResetTokenRepository.delete(resetToken);
            throw new RuntimeException("Reset token expired");
        }

        // recupera utente collegato a quel token
        User user = resetToken.getUser();

        // Cripta la nuova password con BCrypt
        user.setPassword(bcrypt.encode(body.newPassword()));

        // Salva l'utente aggiornato nel database con la nuova password criptata
        userService.saveEntity(user);

        // Il token viene eliminato dopo il cambio password
        // In questo modo il link di reset può essere usato una sola volta
        // e non può essere riutilizzato in futuro per motivi di sicurezza
        passwordResetTokenRepository.delete(resetToken);

    }
}
