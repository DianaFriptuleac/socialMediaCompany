package dianafriptuleac.socialMediaCompany.payloads.Events;

import dianafriptuleac.socialMediaCompany.enums.Events.ParticipationStatus;

import java.util.UUID;

public record EventParticipantViewDTO(
        UUID userId,
        String name,
        String surname,
        String email,
        String avatar,
        ParticipationStatus status
) {
}
