package dianafriptuleac.socialMediaCompany.payloads.search;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventSearchDTO(
        UUID id,
        String name,
        String location,
        LocalDateTime startAt,
        LocalDateTime endAt

) {
}
