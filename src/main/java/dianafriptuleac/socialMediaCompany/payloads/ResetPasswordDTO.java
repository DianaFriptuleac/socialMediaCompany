package dianafriptuleac.socialMediaCompany.payloads;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record ResetPasswordDTO(
        @NotEmpty(message = "Toke is required")
        String token,

        @NotEmpty(message = "Password is required")
        @Size(min = 4, message = "Password must be at least 4 characters long")
        String newPassword
) {
}
