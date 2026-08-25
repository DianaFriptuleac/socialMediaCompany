package dianafriptuleac.socialMediaCompany.payloads.jobs;

import dianafriptuleac.socialMediaCompany.enums.jobs.EmploymentType;
import dianafriptuleac.socialMediaCompany.enums.jobs.WorkMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record JobCreateDTO(
        @NotBlank(message = "Job title is required")    // si usa per le String
        String title,

        @NotBlank(message = "Job description is required")
        String description,

        String requirements,
        
        String location,

        @NotNull          // adatto per enum, UUID, numeri, date, oggetti, ecc.
        EmploymentType employmentType,

        @NotNull
        WorkMode workMode,

        @NotNull
        UUID departmentId,

        LocalDate applicationDeadline
) {
}
