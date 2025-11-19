package dianafriptuleac.socialMediaCompany.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity // Abilita la configurazione Web di Spring Security
@EnableMethodSecurity  //Abilita le annotazioni di sicurezza a livello di metodo (es. @PreAuthorize, @Secured)
public class SecurityConfig {


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, JWTCheckerFilter jwtCheckerFilter) throws Exception {
        //  la catena di filtri di Spring Security

        httpSecurity.formLogin(httpSecurityFormLoginConfigurer -> httpSecurityFormLoginConfigurer.disable());
        // Disabilita il form login standard (pagine /login HTML). Utile per API stateless/JWT

        httpSecurity.csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable());
        // Disabilita CSRF. In REST stateless (senza sessioni e cookie) di solito è disabilitato.
        // (Se si usa cookie/sessione, non va disabilitato CSRF o va configurato correttamente)

        httpSecurity.sessionManagement(httpSecuritySessionManagementConfigurer ->
                httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        //Richiede gestione stateless: niente HttpSession. Le richieste portano le credenziali (es. JWT)

        httpSecurity.authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
                authorizationManagerRequestMatcherRegistry.requestMatchers("/auth/**").permitAll().anyRequest().authenticated());

        httpSecurity.cors(Customizer.withDefaults())
                //Abilita CORS usando la CorsConfigurationSource definita sotto (bean corsConfigurationSource())
                .addFilterBefore(
                        jwtCheckerFilter,
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class
                );
        // Aggiunge il filtro JWT personalizzato prima del filtro standard di login di Spring Security.
        // Per ogni richiesta, il sistema controlla prima se esiste un token Bearer valido
        // (autenticazione tramite JWT) prima di eseguire l’autenticazione classica con username/password.
        return httpSecurity.build();
        // Costruisce e restituisce la SecurityFilterChain
    }

    @Bean
    PasswordEncoder getBCrypt() {
        // Espone un PasswordEncoder a base di BCrypt (cost factor 12)
        // Usato per hashare/verificare password degli utenti
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        //Configurazione CORS (Cross-Origin Resource Sharing)

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000",     // Permette richieste dal frontend in locale
                "http://localhost:5174",
                "https://*.onrender.com",    // Permette sottodomini su onrender.com
                "https://*.vercel.app"));    // Permette sottodomini su vercel.app
        configuration.setAllowedMethods(Arrays.asList(
                "POST", "GET", "PUT", "DELETE", "PATCH", "OPTIONS"
                //Metodi HTTP consentiti dal browser nelle richieste cross-origin
        ));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        //Header consentiti nelle richieste (incluso Authorization per il token JWT)

        configuration.setAllowCredentials(true);
        // Permette l’invio di credenziali (cookie/Authorization header) nelle richieste CORS

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        //Applica la configurazione CORS a tutti gli endpoint.
        return source;
    }
}
