package dianafriptuleac.socialMediaCompany.payloads.Post;

import dianafriptuleac.socialMediaCompany.payloads.Comment.UserPublicDTO;

import java.time.Instant;
import java.util.UUID;

public record PostShareResponseDTO(
        UUID id,
        UUID postId,
        UserPublicDTO sender,
        UserPublicDTO recipient,
        String message,
        Instant createdAt,
        boolean read
) {
}
