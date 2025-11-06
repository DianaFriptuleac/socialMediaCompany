package dianafriptuleac.socialMediaCompany.payloads;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record UserLoginDTO(
        @NotEmpty(message = "Email is required")
        @Email(message = "Invalid email")
        String email,

        @NotEmpty(message = "Password is required")
        @Size(min = 4, message = "Password must be at least 4 characters long")
        String password
) {
}
