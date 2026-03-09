package dianafriptuleac.socialMediaCompany.payloads.messages;

import java.time.Instant;
import java.util.UUID;

public record ConversationListItemDTO(
        UUID id,
        Instant createdAt,
        ConversationUserDTO otherUser,
        String lastMessageText,
        UUID lastMessageSenderId,
        Instant lastMessageCreatedAt,
        long unreadCount
) {
}
