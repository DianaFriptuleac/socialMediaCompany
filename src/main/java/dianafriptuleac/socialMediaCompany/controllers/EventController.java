package dianafriptuleac.socialMediaCompany.controllers;

import dianafriptuleac.socialMediaCompany.entities.Events.Event;
import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.payloads.Events.EventCreateDTO;
import dianafriptuleac.socialMediaCompany.payloads.Events.EventParticipationResponseDTO;
import dianafriptuleac.socialMediaCompany.payloads.Events.EventParticipationUpdateDTO;
import dianafriptuleac.socialMediaCompany.services.EventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/event")
public class EventController {
    @Autowired
    private EventService eventService;

    @PostMapping
    public Event createEvent(
            @RequestBody @Valid EventCreateDTO dto,
            @AuthenticationPrincipal User user
    ) {
        return eventService.createEvent(dto, user);
    }

    @PatchMapping("/{eventId}/participation")
    public EventParticipationResponseDTO updateParticipation(
            @PathVariable java.util.UUID eventId,
            @RequestBody @Valid EventParticipationUpdateDTO dto,
            @AuthenticationPrincipal User user
    ) {
        return eventService.updateParticipation(eventId, user, dto.status());
    }
}
