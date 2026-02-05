package dianafriptuleac.socialMediaCompany.payloads.Post;

import java.util.List;
import java.util.UUID;

public record SharePostDTO(
        List<UUID> recipientIds,
        String message
) {
}
