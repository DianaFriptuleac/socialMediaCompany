package dianafriptuleac.socialMediaCompany.services;

import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.exceptions.UnauthorizedException;
import dianafriptuleac.socialMediaCompany.payloads.UserLoginDTO;
import dianafriptuleac.socialMediaCompany.payloads.UserLoginResponseDTO;
import dianafriptuleac.socialMediaCompany.tools.JWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {
    @Autowired
    private UserService userService;

    @Autowired
    private JWT jwt;

    @Autowired
    private PasswordEncoder bcrypt;

    public UserLoginResponseDTO checkAllCredentialsAndToken(UserLoginDTO body) {
        User userFound = this.userService.findByEmail(body.email());
        if (bcrypt.matches(body.password(), userFound.getPassword())) {
            String accessToken = jwt.createToken(userFound);
            return new UserLoginResponseDTO(
                    accessToken,
                    userFound.getId(),
                    userFound.getName(),
                    userFound.getSurname(),
                    userFound.getEmail(),
                    userFound.getAvatar()
            );
        } else {
            throw new UnauthorizedException("Credenziali utente errate!");
        }
    }
}
