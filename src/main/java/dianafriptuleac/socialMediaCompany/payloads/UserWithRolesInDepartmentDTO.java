package dianafriptuleac.socialMediaCompany.payloads;

import dianafriptuleac.socialMediaCompany.enums.DepartmentRole;

import java.util.List;
import java.util.UUID;

public record UserWithRolesInDepartmentDTO(UUID id,
                                           String name,
                                           String surname,
                                           String email,
                                           String avatar,
                                           List<DepartmentRole> roles) {
}
