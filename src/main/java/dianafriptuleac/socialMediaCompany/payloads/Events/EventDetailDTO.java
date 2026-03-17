package dianafriptuleac.socialMediaCompany.payloads.Events;

import dianafriptuleac.socialMediaCompany.enums.Events.EventAudienceType;
import dianafriptuleac.socialMediaCompany.enums.Events.EventType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EventDetailDTO(
        UUID id,
        String name,
        String location,
        String description,
        LocalDateTime startAt,
        LocalDateTime endAt,
        EventType type,
        EventAudienceType audienceType,
        UUID createdBy,
        String createdByName,
        long totalInvited,
        long acceptedCount,
        long declinedCount,
        long pendingCount,
        List<String> departments,
        List<EventParticipantViewDTO> participants
) {
}
