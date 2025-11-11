package dianafriptuleac.socialMediaCompany.payloads;

import java.util.List;
import java.util.UUID;

public record UserWithDepartmentsDTO(UUID id, String name, String surname, String email,
                                     List<UserDepartmentRolesViewDTO> memberships) {
}
