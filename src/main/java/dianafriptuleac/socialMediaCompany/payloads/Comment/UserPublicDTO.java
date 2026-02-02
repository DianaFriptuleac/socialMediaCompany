package dianafriptuleac.socialMediaCompany.payloads.Comment;

import dianafriptuleac.socialMediaCompany.enums.Role;

import java.util.UUID;

public record UserPublicDTO(
        UUID id,
        String name,
        String surname,
        String avatar,
        Role role
) {
}
