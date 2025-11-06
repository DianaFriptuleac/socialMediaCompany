package dianafriptuleac.socialMediaCompany.payloads;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record UserDTO(
        @NotEmpty(message = "Name is required")
        @Size(min = 3, max = 30, message = "The name must be between 3 and 30 characters")
        String name,

        @NotEmpty(message = "Surname is required")
        @Size(min = 2, max = 30, message = "The surname must be between 2 and 30 characters")
        String surname,

        @NotEmpty(message = "Email is required")
        @Email(message = "Invalid email")
        String email,

        String password
) {
}