package dianafriptuleac.socialMediaCompany.payloads.messages;

import java.util.UUID;

public record ConversationUserDTO(
        UUID id,
        String name,
        String surname,
        String avatar
) {
}
