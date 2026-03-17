package dianafriptuleac.socialMediaCompany.controllers;

import dianafriptuleac.socialMediaCompany.entities.Events.Event;
import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.payloads.Events.EventCreateDTO;
import dianafriptuleac.socialMediaCompany.services.EventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
