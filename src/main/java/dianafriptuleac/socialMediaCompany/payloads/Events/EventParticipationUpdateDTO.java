package dianafriptuleac.socialMediaCompany.payloads.Events;

import dianafriptuleac.socialMediaCompany.enums.Events.ParticipationStatus;
import jakarta.validation.constraints.NotNull;

public record EventParticipationUpdateDTO(
        @NotNull ParticipationStatus status
) {
}
