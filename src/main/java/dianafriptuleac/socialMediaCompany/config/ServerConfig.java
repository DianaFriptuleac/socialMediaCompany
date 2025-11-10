package dianafriptuleac.socialMediaCompany.config;


import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ServerConfig {

    // CONFIGURAZIONE DEL CLIENT CLOUDINARY
    @Bean
    // Registra un bean di tipo Cloudinary nel contesto Spring
    // Ogni volta che un componente ha bisogno di Cloudinary, Spring fornirà questa istanza

    public Cloudinary cloudinary(@Value("${cloudinary.name}") String cloudName,
                                 @Value("${cloudinary.key}") String apiKey,
                                 @Value("${cloudinary.secret}") String apiSecret) {
        // Recupera dal file application.properties (o .env) le 3 variabili:
        // cloudinary.name, cloudinary.key, cloudinary.secret

        Map<String, String> config = new HashMap<>();
        //Crea una mappa per passare i parametri di configurazione a Cloudinary

        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        // Inserisce nella mappa i valori della configurazione

        return new Cloudinary(config);
        // Crea e restituisce un oggetto Cloudinary configurato → verrà usato nei servizi (es. UserService) per caricare immagini
    }


    // CONFIGURAZIONE DI UN CLIENT HTTP (PER CHIAMATE ESTERNE)
    @Bean
    public RestTemplate restTemplate() {
        // Restituisce un’istanza di RestTemplate, la classe standard di Spring
        // per effettuare chiamate HTTP verso API esterne (es. GET, POST, PUT, DELETE)
        return new RestTemplate();
    }


    // CONFIGURAZIONE PER LA GESTIONE DI FILE MULTIPART (UPLOAD)
    @Bean
    public MultipartResolver multipartResolver() {
        // Crea e registra un bean StandardServletMultipartResolver,
        // che serve a Spring per gestire le richieste HTTP contenenti file (multipart/form-data) ->
        // supporta l’upload di file (es. immagini, documenti, ecc.) nei controller (avatar)
        return new StandardServletMultipartResolver();
    }
}
