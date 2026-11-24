package dianafriptuleac.socialMediaCompany.config;

import dianafriptuleac.socialMediaCompany.enums.Role;
import dianafriptuleac.socialMediaCompany.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminInitializer {

    @Bean
    public CommandLineRunner initializeAdmins(
            UserRepository userRepository,
            @Value("${ADMIN_EMAILS:}") String adminEmails) {

        return args -> {

            if (adminEmails.isBlank()) {
                return;
            }

            for (String email : adminEmails.split(",")) {

                userRepository.findByEmail(email.trim())
                        .ifPresentOrElse(user -> {

                            if (user.getRole() != Role.ADMIN) {
                                user.setRole(Role.ADMIN);
                                userRepository.save(user);

                                System.out.println(
                                        "Admin role assigned: " + user.getEmail()
                                );
                            }

                        }, () -> System.out.println(
                                "User not found: " + email.trim()
                        ));
            }
        };
    }
}