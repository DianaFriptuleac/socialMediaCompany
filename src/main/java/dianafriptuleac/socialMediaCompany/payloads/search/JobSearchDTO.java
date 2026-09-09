package dianafriptuleac.socialMediaCompany.payloads.search;

import java.time.LocalDate;
import java.util.UUID;

public record JobSearchDTO(
        UUID id,
        String title,
        String location,
        String departmentName,
        LocalDate applicationDeadline
) {
}
