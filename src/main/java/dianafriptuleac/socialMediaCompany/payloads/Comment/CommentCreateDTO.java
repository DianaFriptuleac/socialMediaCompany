package dianafriptuleac.socialMediaCompany.payloads.Comment;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CommentCreateDTO(
        @NotBlank String text, UUID parentCommentId
) {
}
