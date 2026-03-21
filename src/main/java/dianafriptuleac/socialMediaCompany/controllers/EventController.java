package dianafriptuleac.socialMediaCompany.controllers;

import dianafriptuleac.socialMediaCompany.entities.Events.Event;
import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.payloads.Events.*;
import dianafriptuleac.socialMediaCompany.services.EventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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

    // get List
    @GetMapping
    public Page<EventListDTO> getAllEvents(
            @PageableDefault(size = 10, sort = "StartAt") Pageable pageable
    ) {
        return eventService.getAllEvents(pageable);
    }

    //Get event by id
    @GetMapping("/{eventId}")
    public EventDetailDTO getEventById(@PathVariable UUID eventId) {
        return eventService.getEventById(eventId);
    }

    @PatchMapping("/{eventId}/participation")
    public EventParticipationResponseDTO updateParticipation(
            @PathVariable UUID eventId,
            @RequestBody @Valid EventParticipationUpdateDTO dto,
            @AuthenticationPrincipal User user
    ) {
        return eventService.updateParticipation(eventId, user, dto.status());
    }

}
