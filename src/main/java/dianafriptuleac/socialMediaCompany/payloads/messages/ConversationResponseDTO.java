package dianafriptuleac.socialMediaCompany.payloads.messages;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ConversationResponseDTO(
        UUID id,
        List<UUID> participantIds,
        Instant createdAt
) {
}
