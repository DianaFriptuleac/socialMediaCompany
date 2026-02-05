package dianafriptuleac.socialMediaCompany.payloads.Comment;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CommentResponseDTO(
        UUID id,
        UUID postId,
        UserPublicDTO author,
        String text,
        UUID parentCommentId,
        List<CommentResponseDTO> replies,
        Instant createdAt,
        Instant updatedAt
) {
}
