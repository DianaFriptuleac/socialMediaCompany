package dianafriptuleac.socialMediaCompany.payloads.search;

import java.util.UUID;

public record DepartmentSearchDTO(
        UUID id,
        String name,
        String description
) {
}
