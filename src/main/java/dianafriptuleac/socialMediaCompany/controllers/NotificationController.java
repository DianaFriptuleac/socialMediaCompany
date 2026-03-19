package dianafriptuleac.socialMediaCompany.controllers;

import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.payloads.NotificationDTO;
import dianafriptuleac.socialMediaCompany.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/me")
    public List<NotificationDTO> getMyNotifications(@AuthenticationPrincipal User user) {
        return notificationService.getMyNotifications(user);
    }

    @PatchMapping("/{notificationId}/read")
    public void markAsRead(@PathVariable java.util.UUID notificationId,
                           @AuthenticationPrincipal User user) {
        notificationService.markAsRead(notificationId, user);
    }
}
