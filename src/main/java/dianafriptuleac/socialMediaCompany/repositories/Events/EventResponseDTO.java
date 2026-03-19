package dianafriptuleac.socialMediaCompany.repositories.Events;

import dianafriptuleac.socialMediaCompany.enums.Events.EventAudienceType;
import dianafriptuleac.socialMediaCompany.enums.Events.EventType;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventResponseDTO(
        UUID id,
        String name,
        String location,
        String description,
        LocalDateTime startAt,
        LocalDateTime endAt,
        EventType type,
        EventAudienceType audienceType,
        UUID createdById
) {
}