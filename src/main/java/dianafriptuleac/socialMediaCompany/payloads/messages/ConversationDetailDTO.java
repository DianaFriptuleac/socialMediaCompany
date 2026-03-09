package dianafriptuleac.socialMediaCompany.payloads.messages;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ConversationDetailDTO(
        UUID id,
        Instant createdAt,
        List<ConversationUserDTO> participants
) {
}
