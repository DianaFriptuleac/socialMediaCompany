package dianafriptuleac.socialMediaCompany.payloads;

import dianafriptuleac.socialMediaCompany.enums.DepartmentRole;

import java.util.List;
import java.util.UUID;

public record AssignRoleDTO(UUID userId, UUID departmentId, List<DepartmentRole> roles) {
}
