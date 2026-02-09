package dianafriptuleac.socialMediaCompany.payloads.messages;

import java.util.UUID;

public record SendMessageRequestDTO(
        UUID conversationId,
        String text,
        UUID replyToMessageId
) {
}
