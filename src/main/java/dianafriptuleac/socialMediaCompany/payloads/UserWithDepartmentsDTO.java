package dianafriptuleac.socialMediaCompany.payloads;

import java.util.UUID;

public record UserWithDepartmentsDTO(UUID id, String name, String surname, String email,
                                     java.util.List<UserDepartmentRolesViewDTO> memberships) {
}
