package dianafriptuleac.socialMediaCompany.payloads.Comment;

import dianafriptuleac.socialMediaCompany.payloads.UserLoginResponseDTO;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CommentResponseDTO(
        UUID id,
        UUID postId,
        UserLoginResponseDTO author,
        String text,
        UUID parentCommentId,
        List<CommentResponseDTO> replies,
        Instant createdAt,
        Instant updatedAt
) {
}
