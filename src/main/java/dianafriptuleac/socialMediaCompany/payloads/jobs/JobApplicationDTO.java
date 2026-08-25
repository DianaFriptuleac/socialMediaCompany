package dianafriptuleac.socialMediaCompany.payloads.jobs;

import dianafriptuleac.socialMediaCompany.enums.jobs.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record JobApplicationDTO(
        UUID id,
        UUID jobId,
        String jobTitle,
        UUID applicantId,
        String applicantName,
        String cvUrl,
        String coverLetterText,
        String coverLetterUrl,
        ApplicationStatus status,
        LocalDateTime appliedAt
) {
}
