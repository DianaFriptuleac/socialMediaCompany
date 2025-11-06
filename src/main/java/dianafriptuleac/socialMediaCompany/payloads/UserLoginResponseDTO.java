package dianafriptuleac.socialMediaCompany.payloads;

import java.util.UUID;

public record UserLoginResponseDTO(String accessToken,
                                   UUID id, String name, String surname, String email, String avatar) {
}
