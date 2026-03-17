package dianafriptuleac.socialMediaCompany.payloads.Events;

import dianafriptuleac.socialMediaCompany.enums.Events.ParticipationStatus;

public record EventParticipationResponseDTO(
        ParticipationStatus status
) {
}
