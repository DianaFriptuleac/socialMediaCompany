package dianafriptuleac.socialMediaCompany.services;

import dianafriptuleac.socialMediaCompany.entities.Department;
import dianafriptuleac.socialMediaCompany.entities.Events.Event;
import dianafriptuleac.socialMediaCompany.entities.Events.EventDepartment;
import dianafriptuleac.socialMediaCompany.entities.Events.EventParticipant;
import dianafriptuleac.socialMediaCompany.entities.Notification;
import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.enums.Events.EventAudienceType;
import dianafriptuleac.socialMediaCompany.enums.Events.ParticipationStatus;
import dianafriptuleac.socialMediaCompany.exceptions.BadRequestException;
import dianafriptuleac.socialMediaCompany.exceptions.NotFoundException;
import dianafriptuleac.socialMediaCompany.payloads.Events.*;
import dianafriptuleac.socialMediaCompany.repositories.DepartmentRepository;
import dianafriptuleac.socialMediaCompany.repositories.Events.EventDepartmentRepository;
import dianafriptuleac.socialMediaCompany.repositories.Events.EventParticipantRepository;
import dianafriptuleac.socialMediaCompany.repositories.Events.EventRepository;
import dianafriptuleac.socialMediaCompany.repositories.NotificationRepository;
import dianafriptuleac.socialMediaCompany.repositories.UserDepartmentRoleRepository;
import dianafriptuleac.socialMediaCompany.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private EventDepartmentRepository eventDepartmentRepository;
    @Autowired
    private EventParticipantRepository eventParticipantRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserDepartmentRoleRepository userDepartmentRoleRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    // se qualcosa fallisce - rollback automatico
    public Event createEvent(EventCreateDTO dto, User creator) {

        // Controlli business
        if (dto.audienceType() == EventAudienceType.DEPARTMENTS &&
                (dto.departmentIds() == null || dto.departmentIds().isEmpty())) {
            throw new BadRequestException("Departments required");
        }

        if (dto.audienceType() == EventAudienceType.SPECIFIC_USERS &&
                (dto.userIds() == null || dto.userIds().isEmpty())) {
            throw new BadRequestException("Users required");
        }

        if (dto.audienceType() == EventAudienceType.MIXED) {
            boolean noDept = dto.departmentIds() == null || dto.departmentIds().isEmpty();
            boolean noUsers = dto.userIds() == null || dto.userIds().isEmpty();

            if (noDept && noUsers) {
                throw new BadRequestException("Users or departments required");
            }
        }

        //Creazione evento

        Event event = new Event();
        event.setName(dto.name().trim());
        event.setLocation(dto.location());
        event.setDescription(dto.description());
        event.setStartAt(dto.startAt());
        event.setEndAt(dto.endAt());
        event.setType(dto.type());
        event.setAudienceType(dto.audienceType());
        event.setCreatedBy(creator);

        Event savedEvent = eventRepository.save(event);

        //Raccolta eventi

        Set<UUID> userIds = new HashSet<>();

        // utenti dai reparti
        if (dto.departmentIds() != null && !dto.departmentIds().isEmpty()) {

            List<Department> departments = departmentRepository.findAllById(dto.departmentIds());

            if (departments.size() != dto.departmentIds().size()) {
                throw new NotFoundException("Department not found");
            }

            // salvo relazione evento-reparto
            for (Department d : departments) {
                eventDepartmentRepository.save(
                        EventDepartment.builder()
                                .event(savedEvent)
                                .department(d)
                                .build()
                );
            }

            // prendo utenti dei reparti
            userIds.addAll(
                    userDepartmentRoleRepository.findDistinctUserIdsByDepartmentIds(dto.departmentIds())
            );
        }

        // utenti diretti
        if (dto.userIds() != null) {
            userIds.addAll(dto.userIds());
        }

        // tutti gli utenti
        if (dto.audienceType() == EventAudienceType.ALL_EMPLOYEES) {
            userRepository.findAll().forEach(u -> userIds.add(u.getId()));
        }


        //Creazione partecipanti

        List<User> users = userRepository.findAllById(userIds);

        List<EventParticipant> participants = users.stream()
                .map(user -> EventParticipant.builder()
                        .event(savedEvent)
                        .user(user)
                        .status(ParticipationStatus.PENDING)
                        .invitedDirectly(
                                dto.userIds() != null && dto.userIds().contains(user.getId())
                        )
                        .invitedThroughDepartment(
                                dto.departmentIds() != null && !dto.departmentIds().isEmpty()
                        )
                        .build())
                .toList();

        eventParticipantRepository.saveAll(participants);

        // Manda notifica evento allo user
        List<Notification> notifications = users.stream()
                .map(user -> Notification.builder()
                        .user(user)
                        .title("New Event")
                        .message("You have been invited to the event: " + savedEvent.getName())
                        .createdAt(LocalDateTime.now())
                        .eventId(savedEvent.getId())
                        .type("Event_Invitation")
                        .build())
                .toList();
        notificationRepository.saveAll(notifications);

        return savedEvent;
    }

    @Transactional
    public EventParticipationResponseDTO updateParticipation(UUID eventId, User user, ParticipationStatus status) {
        if (status == ParticipationStatus.PENDING) {
            throw new BadRequestException("Invalid participation status");
        }
        EventParticipant participant = eventParticipantRepository
                .findByEventIdAndUserId(eventId, user.getId())
                .orElseThrow(() -> new NotFoundException("Invitation not found for this user"));

        participant.setStatus(status);
        participant.setRespondedAt(java.time.LocalDateTime.now());

        eventParticipantRepository.save(participant);

        return new EventParticipationResponseDTO(participant.getStatus());
    }

    //Helper buildUserFullName
    private String buildUserFullName(User user) {
        String name = user.getName() != null ? user.getName() : "";
        String surname = user.getSurname() != null ? user.getSurname() : "";
        return (name + " " + surname).trim();
    }

    // Lista events
    @Transactional(readOnly = true)
    public Page<EventListDTO> getAllEvents(Pageable pageable) {
        Page<Event> eventsPage = eventRepository.findAllByOrderByStartAtDesc(pageable);

        return eventsPage
                .map(event -> new EventListDTO(
                        event.getId(),
                        event.getName(),
                        event.getLocation(),
                        event.getStartAt(),
                        event.getEndAt(),
                        event.getType(),
                        event.getAudienceType(),
                        event.getCreatedBy().getId(),
                        buildUserFullName(event.getCreatedBy()),
                        eventParticipantRepository.countByEventId(event.getId()),
                        eventParticipantRepository.countByEventIdAndStatus(event.getId(), ParticipationStatus.ACCEPTED),
                        eventParticipantRepository.countByEventIdAndStatus(event.getId(), ParticipationStatus.DECLINED),
                        eventParticipantRepository.countByEventIdAndStatus(event.getId(), ParticipationStatus.PENDING)
                ));
    }

    //get Event by id
    @Transactional(readOnly = true)
    public EventDetailDTO getEventById(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        List<EventParticipant> participants = eventParticipantRepository.findByEventId(eventId);
        List<EventDepartment> departments = eventDepartmentRepository.findByEventId(eventId);

        List<EventParticipantViewDTO> participantDtos = participants.stream()
                .map(p -> new EventParticipantViewDTO(
                        p.getUser().getId(),
                        p.getUser().getName(),
                        p.getUser().getSurname(),
                        p.getUser().getEmail(),
                        p.getUser().getAvatar(),
                        p.getStatus()
                ))
                .toList();
        List<String> departmentNames = departments.stream()
                .map(ed -> ed.getDepartment().getName())
                .toList();
        long totalInvited = participants.size();
        long acceptedCount = participants.stream().filter(p -> p.getStatus() == ParticipationStatus.ACCEPTED).count();
        long declinedCount = participants.stream().filter(p -> p.getStatus() == ParticipationStatus.DECLINED).count();
        long pendingCount = participants.stream().filter(p -> p.getStatus() == ParticipationStatus.PENDING).count();

        return new EventDetailDTO(
                event.getId(),
                event.getName(),
                event.getLocation(),
                event.getDescription(),
                event.getStartAt(),
                event.getEndAt(),
                event.getType(),
                event.getAudienceType(),
                event.getCreatedBy().getId(),
                buildUserFullName(event.getCreatedBy()),
                totalInvited,
                acceptedCount,
                declinedCount,
                pendingCount,
                departmentNames,
                participantDtos
        );
    }
}
