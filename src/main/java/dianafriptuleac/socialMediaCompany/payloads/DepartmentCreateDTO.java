package dianafriptuleac.socialMediaCompany.payloads;

import jakarta.validation.constraints.NotEmpty;

public record DepartmentCreateDTO(
        @NotEmpty(message = "Department name is required")
        String name,

        @NotEmpty(message = "Department description is required")
        String description) {
}
