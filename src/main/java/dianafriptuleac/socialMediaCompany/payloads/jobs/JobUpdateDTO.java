package dianafriptuleac.socialMediaCompany.payloads.jobs;

import dianafriptuleac.socialMediaCompany.enums.jobs.EmploymentType;
import dianafriptuleac.socialMediaCompany.enums.jobs.WorkMode;

import java.time.LocalDate;
import java.util.UUID;

public record JobUpdateDTO(
        String title,

        String description,

        String requirements,

        String location,

        EmploymentType employmentType,

        WorkMode workMode,

        UUID departmentId,

        LocalDate applicationDeadline
) {
}
