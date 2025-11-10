package dianafriptuleac.socialMediaCompany.payloads;

import dianafriptuleac.socialMediaCompany.enums.DepartmentRole;

import java.util.UUID;

public record UserDepartmentRolesViewDTO(UUID departmentId, String departmentName,
                                         java.util.List<DepartmentRole> roles) {
}
