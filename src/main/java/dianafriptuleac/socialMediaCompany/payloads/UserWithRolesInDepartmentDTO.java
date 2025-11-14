package dianafriptuleac.socialMediaCompany.payloads;

import java.util.List;
import java.util.UUID;

public record UserWithRolesInDepartmentDTO(UUID id,
                                           String name,
                                           String surname,
                                           String email,
                                           String avatar,
                                           List<String> roles) {
}
