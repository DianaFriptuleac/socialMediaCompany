package dianafriptuleac.socialMediaCompany.payloads.jobs;

import dianafriptuleac.socialMediaCompany.enums.jobs.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record ApplicationStatusDTO(
        @NotNull
        ApplicationStatus status
) {
}
