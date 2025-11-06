package dianafriptuleac.socialMediaCompany.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.enums.Role;
import dianafriptuleac.socialMediaCompany.exceptions.BadRequestException;
import dianafriptuleac.socialMediaCompany.exceptions.NotFoundException;
import dianafriptuleac.socialMediaCompany.payloads.UserDTO;
import dianafriptuleac.socialMediaCompany.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder bcrypt;

    @Autowired
    private Cloudinary cloudinary;

    public User save(UserDTO body) {
        this.userRepository.findByEmail(body.email()).ifPresent(
                utente -> {
                    throw new BadRequestException("Email " + body.email() + " già in uso!");
                }
        );
        User newUser = new User(body.name(), body.surname(), body.email(),
                bcrypt.encode(body.password()),
                "https://ui-avatars.com/api/?name=" + body.name() + "+" + body.surname(), Role.USER);
        return this.userRepository.save(newUser);
    }

    public Page<User> findAll(int page, int size, String sortBy) {
        if (size > 100)
            size = 100;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return this.userRepository.findAll(pageable);
    }

    public User findById(UUID utenteId) {
        return this.userRepository.findById(utenteId)
                .orElseThrow(() -> new NotFoundException("Utente con ID " + utenteId + " non trovato"));
    }

    public User findByEmail(String email) {
        return this.userRepository.findByEmail(email).orElseThrow(() ->
                new NotFoundException("L'utente con email " + email + " non è stato trovato"));
    }

    public User findByIdAndUpdate(UUID utenteId, UserDTO body) {
        User foundUtente = this.findById(utenteId);

        if (body.email() != null && !foundUtente.getEmail().equals(body.email())) {
            this.userRepository.findByEmail(body.email()).ifPresent(
                    utente -> {
                        throw new BadRequestException("Email " + body.email() + " è già in uso!");
                    }
            );
            foundUtente.setEmail(body.email());
        }

        // Aggiorno nome se presente
        if (body.name() != null) {
            foundUtente.setName(body.name());
        }

        if (body.surname() != null) {
            foundUtente.setSurname(body.surname());
        }

        if (body.password() != null && !body.password().isEmpty()) {
            foundUtente.setPassword(bcrypt.encode(body.password()));
        }

        return this.userRepository.save(foundUtente);
    }


    public void findByIdAndDelete(UUID userId) {
        User foundUser = this.findById(userId);
        this.userRepository.delete(foundUser);
    }

    public Map<String, String> uploadAvatar(UUID usertId, MultipartFile file) {
        System.out.println("File ricevuto: " + file.getOriginalFilename());
        System.out.println("Content-Type: " + file.getContentType());
        System.out.println("Dimensione: " + file.getSize());
        User user = findById(usertId);
        String url;

        try {
            url = (String) cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap()).get("url");
            user.setAvatar(url);
            userRepository.save(user);
        } catch (IOException e) {
            throw new BadRequestException("Errore durante l'upload dell'immagine.");
        }

        Map<String, String> response = new HashMap<>();
        response.put("avatarUrl", url);
        return response;
    }

}
