package dianafriptuleac.socialMediaCompany.payloads.messages;

import java.time.Instant;
import java.util.UUID;

public record MessageResponseDTO(
        UUID id,
        UUID conversationId,
        UUID senderId,
        String text,
        UUID replyToId,
        Instant createdAt,
        Instant readAt
) {
}
