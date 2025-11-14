package dianafriptuleac.socialMediaCompany.payloads;

import java.util.List;
import java.util.UUID;

public record DepartmentWithUserDTO(UUID id,
                                    String name,
                                    String description,
                                    int userCount,
                                    List<UserWithRolesInDepartmentDTO> users) {
}
