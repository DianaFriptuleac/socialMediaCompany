package dianafriptuleac.socialMediaCompany.payloads.messages;

import java.util.UUID;

public record CreateConversationRequestDTO(
        UUID otherUserId
) {
}
