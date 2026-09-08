package dianafriptuleac.socialMediaCompany.services;

import dianafriptuleac.socialMediaCompany.entities.Notification;
import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.exceptions.NotFoundException;
import dianafriptuleac.socialMediaCompany.exceptions.UnauthorizedException;
import dianafriptuleac.socialMediaCompany.payloads.NotificationDTO;
import dianafriptuleac.socialMediaCompany.repositories.NotificationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EventService eventService;

    public List<NotificationDTO> getMyNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public void markAsRead(UUID notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new NotFoundException("Notification not fount");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void deleteNotification(UUID notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not allowed to delete this notification");
        }
        notificationRepository.delete(notification);
    }

    // --------- crea notifica
    public Notification createNotification(User user, String title, String message, UUID referenceId, String type) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .read(false)
                .createdAt(LocalDateTime.now())
                // eventId ora si riferisce anche alla notifica dei jobs
                .eventId(referenceId)
                .type(type)
                .build();
        return notificationRepository.save(notification);
    }

    private NotificationDTO mapToDTO(Notification notification) {

        boolean targetAvailable = true;
        boolean isEventNotification = notification.getType() != null
                && (
                notification.getType().startsWith("EVENT_")
                        || notification.getType().startsWith("Event_")
        );

        if (isEventNotification && notification.getEventId() != null) {
            targetAvailable = eventService.isEventAvailable(notification.getEventId());
        }
        return new NotificationDTO(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getEventId(),
                notification.getType(),
                targetAvailable
        );
    }
}
