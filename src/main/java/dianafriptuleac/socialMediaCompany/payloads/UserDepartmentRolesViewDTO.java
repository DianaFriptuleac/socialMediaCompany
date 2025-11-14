package dianafriptuleac.socialMediaCompany.payloads;

import java.util.List;
import java.util.UUID;

public record UserDepartmentRolesViewDTO(UUID departmentId, String departmentName,
                                         List<String> roles) {
}
