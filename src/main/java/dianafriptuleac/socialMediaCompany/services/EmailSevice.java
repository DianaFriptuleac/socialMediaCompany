package dianafriptuleac.socialMediaCompany.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSevice {
    @Autowired
    // JavaMailSender è il componente che si occupa di collegarsi
    // al server SMTP (Brevo) e inviare le email
    private JavaMailSender javaMailSender;

    // to = email destinatario
    // resetLink = link che contiene il token per il reset password
    public void sendPasswordResetEmail(String to, String resetLink) {

        // Crea un nuovo messaggio email vuoto
        SimpleMailMessage message = new SimpleMailMessage();
        // mittente della mail
        message.setFrom("dianadorojuc3@gmail.com");   // mail da modificare
        // destinatario
        message.setTo(to);
        //l'oggetto della mail
        message.setSubject("Reset your EcoMotors password");
        //contenuto mail
        message.setText("""
                Hello,
                
                You requested to reset your password.
                
                Click this link to create a new password:
                %s
                
                This link expires in 30 minutes.
                
                If you did not request this, ignore this email.
                """.formatted(resetLink));     // %s è un placeholder che verrà sostituito dal valore
        // di resetLink tramite .formatted(resetLink)

        // Invia la mail:
        // 1. Spring usa JavaMailSender
        // 2. JavaMailSender si collega a Brevo
        // 3. Effettua il login con username e password SMTP
        // 4. Invia il messaggio
        // 5. Brevo consegna la mail al destinatario
        javaMailSender.send(message);
    }
}
