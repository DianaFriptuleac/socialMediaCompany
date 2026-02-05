package dianafriptuleac.socialMediaCompany.payloads.Post;

import dianafriptuleac.socialMediaCompany.payloads.Comment.UserPublicDTO;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PostResponseDTO(
        UUID id,
        UserPublicDTO author,
        String title,
        String description,
        String externalUrl,
        List<PostMediaDTO> media,
        long likeCount,
        long commentCount,
        boolean likedByMe,
        Instant createdAt,
        Instant updatedAt
) {

}
