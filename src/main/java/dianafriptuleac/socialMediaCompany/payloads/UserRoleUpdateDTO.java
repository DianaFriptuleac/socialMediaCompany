package dianafriptuleac.socialMediaCompany.payloads;

import dianafriptuleac.socialMediaCompany.enums.Role;
import jakarta.validation.constraints.NotNull;

public record UserRoleUpdateDTO(@NotNull(message = "Role is required")
                                Role role) {
}
