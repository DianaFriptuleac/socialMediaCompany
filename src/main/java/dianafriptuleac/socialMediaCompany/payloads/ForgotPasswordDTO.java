package dianafriptuleac.socialMediaCompany.payloads;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record ForgotPasswordDTO(
        @NotEmpty(message = "Email is required")
        @Email(message = "Invalid email")
        String email
) {
}
