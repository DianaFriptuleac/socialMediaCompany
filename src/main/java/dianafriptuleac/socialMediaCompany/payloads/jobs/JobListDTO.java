package dianafriptuleac.socialMediaCompany.payloads.jobs;

import dianafriptuleac.socialMediaCompany.enums.jobs.EmploymentType;
import dianafriptuleac.socialMediaCompany.enums.jobs.WorkMode;

import java.time.LocalDate;
import java.util.UUID;

public record JobListDTO(
        UUID id,
        String title,
        String department,
        String location,
        EmploymentType employmentType,
        WorkMode workMode,
        LocalDate applicationDeadline
) {
}
