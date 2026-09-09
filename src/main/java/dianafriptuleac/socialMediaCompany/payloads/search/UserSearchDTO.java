package dianafriptuleac.socialMediaCompany.payloads.search;

import java.util.UUID;

public record UserSearchDTO(
        UUID id,
        String name,
        String surname,
        String email,
        String avatar
) {
}
