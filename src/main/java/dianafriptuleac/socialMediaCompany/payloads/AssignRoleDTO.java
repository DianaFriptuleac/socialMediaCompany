package dianafriptuleac.socialMediaCompany.payloads;

import java.util.List;
import java.util.UUID;

public record AssignRoleDTO(UUID userId, UUID departmentId, List<String> roles) {
}
