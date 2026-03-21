package dianafriptuleac.socialMediaCompany.payloads.Events;

import dianafriptuleac.socialMediaCompany.enums.Events.EventAudienceType;
import dianafriptuleac.socialMediaCompany.enums.Events.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EventUpdateDTO(

        @NotBlank(message = "Event name is required")
        @Size(max = 150, message = "Event name must be at most 150 characters")
        String name,

        @Size(max = 200, message = "Location must be at most 200 characters")
        String location,

        @Size(max = 1000, message = "Description must be at most 1000 characters")
        String description,

        @NotNull(message = "Start date is required")
        LocalDateTime startAt,

        @NotNull(message = "End date is required")
        LocalDateTime endAt,

        @NotNull(message = "Event type is required")
        EventType type,

        @NotNull(message = "Audience type is required")
        EventAudienceType audienceType,

        List<UUID> departmentIds,
        List<UUID> userIds
) {
}
