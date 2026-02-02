package dianafriptuleac.socialMediaCompany.payloads.Post;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record SharePostDTO(
        @NotEmpty List<UUID> recipientIds,
        String message
) {
}
