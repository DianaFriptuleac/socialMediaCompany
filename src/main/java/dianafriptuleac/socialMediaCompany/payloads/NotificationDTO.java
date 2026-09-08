package dianafriptuleac.socialMediaCompany.payloads;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDTO(
        UUID id,
        String title,
        String message,
        boolean read,
        LocalDateTime createdAt,
        UUID eventId,
        String type,
        boolean targetAvailable
) {
}