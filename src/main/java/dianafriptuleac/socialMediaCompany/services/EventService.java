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
import java.util.*;
import java.util.stream.Collectors;

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

    //Update Event
    @Transactional
    public EventResponseDTO updateEvent(UUID eventId, EventUpdateDTO dto, User currentUser) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        //solo il creator può modificare l'evento
        if (!event.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Only the creator can update this event");
        }

        // per DEPARTMENTS servono reparti
        if (dto.audienceType() == EventAudienceType.DEPARTMENTS &&
                (dto.departmentIds() == null || dto.departmentIds().isEmpty())) {
            throw new BadRequestException("Departments required");
        }
        // per SPECIFIC_USERS servono utenti
        if (dto.audienceType() == EventAudienceType.SPECIFIC_USERS &&
                (dto.userIds() == null || dto.userIds().isEmpty())) {
            throw new BadRequestException("Users required");
        }
        // per MIXED serve almeno un reparto o un utente
        if (dto.audienceType() == EventAudienceType.MIXED) {
            boolean noDept = dto.departmentIds() == null || dto.departmentIds().isEmpty();
            boolean noUsers = dto.userIds() == null || dto.userIds().isEmpty();
            if (noDept && noUsers) {
                throw new BadRequestException("Users or departments required");
            }
        }
        // Validazione sulle date
        if (dto.startAt().isAfter(dto.endAt()) || dto.startAt().isEqual(dto.endAt())) {
            throw new BadRequestException("Start date must be before end date");
        }

        //1. aggiorno dati base evento
        event.setName(dto.name().trim());
        event.setLocation(dto.location());
        event.setDescription(dto.description());
        event.setStartAt(dto.startAt());
        event.setEndAt(dto.endAt());
        event.setType(dto.type());
        event.setAudienceType(dto.audienceType());


        // 2. leggo reparti attuali
        List<EventDepartment> currentEventDepartments = eventDepartmentRepository.findByEventId(eventId);
        Set<UUID> currentDepartmentIds = currentEventDepartments.stream()
                .map(ed -> ed.getDepartment().getId())
                .collect(Collectors.toSet());

        // 3. preparo reparti nuovi
        Set<UUID> newDepartmentIds = dto.departmentIds() != null
                ? new HashSet<>(dto.departmentIds())
                : new HashSet<>();

        // validazione reparti
        if (!newDepartmentIds.isEmpty()) {
            List<Department> departments = departmentRepository.findAllById(newDepartmentIds);
            if (departments.size() != newDepartmentIds.size()) {
                throw new NotFoundException("Department not found");
            }
        }

        // 4. calcolo reparti da aggiungere e rimuovere
        Set<UUID> departmentIdsToAdd = new HashSet<>(newDepartmentIds);
        departmentIdsToAdd.removeAll(currentDepartmentIds);

        Set<UUID> departmentIdsToRemove = new HashSet<>(currentDepartmentIds);
        departmentIdsToRemove.removeAll(newDepartmentIds);

        // 5. rimuovo solo relazioni reparto non più valide
        List<EventDepartment> departmentsToRemove = currentEventDepartments.stream()
                .filter(ed -> departmentIdsToRemove.contains(ed.getDepartment().getId()))
                .toList();
        eventDepartmentRepository.deleteAll(departmentsToRemove);

        // 6. aggiungo solo relazioni nuove
        if (!departmentIdsToAdd.isEmpty()) {
            List<Department> departmentsToAdd = departmentRepository.findAllById(departmentIdsToAdd);
            List<EventDepartment> newEventDepartments = departmentsToAdd.stream()
                    .map(dept -> EventDepartment.builder()
                            .event(event)
                            .department(dept)
                            .build())
                    .toList();
            eventDepartmentRepository.saveAll(newEventDepartments);
        }

        // 7. costruisco utenti finali desiderati
        Set<UUID> desiredUserIds = new HashSet<>();

        if (dto.audienceType() == EventAudienceType.ALL_EMPLOYEES) {
            userRepository.findAll().forEach(u -> desiredUserIds.add(u.getId()));
        }

        if (!newDepartmentIds.isEmpty()) {
            desiredUserIds.addAll(
                    userDepartmentRoleRepository.findDistinctUserIdsByDepartmentIds(List.copyOf(newDepartmentIds))
            );
        }

        if (dto.userIds() != null && !dto.userIds().isEmpty()) {
            desiredUserIds.addAll(dto.userIds());
        }

        // 8. leggo partecipanti attuali
        List<EventParticipant> currentParticipants = eventParticipantRepository.findByEventId(eventId);
        Map<UUID, EventParticipant> currentParticipantsByUserId = currentParticipants.stream()
                .collect(Collectors.toMap(p -> p.getUser().getId(), p -> p));

        Set<UUID> currentParticipantUserIds = currentParticipantsByUserId.keySet();

        // 9. calcolo utenti da aggiungere e rimuovere
        Set<UUID> userIdsToAdd = new HashSet<>(desiredUserIds);
        userIdsToAdd.removeAll(currentParticipantUserIds);

        Set<UUID> userIdsToRemove = new HashSet<>(currentParticipantUserIds);
        userIdsToRemove.removeAll(desiredUserIds);

        // 10. rimuovo solo participant che non devono più esserci
        List<EventParticipant> participantsToRemove = currentParticipants.stream()
                .filter(p -> userIdsToRemove.contains(p.getUser().getId()))
                .toList();
        eventParticipantRepository.deleteAll(participantsToRemove);

        // 11. aggiungo solo nuovi participant
        if (!userIdsToAdd.isEmpty()) {
            List<User> usersToAdd = userRepository.findAllById(userIdsToAdd);

            List<EventParticipant> participantsToAdd = usersToAdd.stream()
                    .map(targetUser -> EventParticipant.builder()
                            .event(event)
                            .user(targetUser)
                            .status(ParticipationStatus.PENDING)
                            .invitedDirectly(dto.userIds() != null && dto.userIds().contains(targetUser.getId()))
                            .invitedThroughDepartment(!newDepartmentIds.isEmpty())
                            .build())
                    .toList();

            eventParticipantRepository.saveAll(participantsToAdd);

            List<Notification> notifications = usersToAdd.stream()
                    .map(u -> Notification.builder()
                            .user(u)
                            .title("Event Updated")
                            .message("You have been invited to the event: " + event.getName())
                            .createdAt(LocalDateTime.now())
                            .eventId(event.getId())
                            .type("EVENT_INVITATION")
                            .build())
                    .toList();

            notificationRepository.saveAll(notifications);
        }

        // 12. aggiorno invitedDirectly / invitedThroughDepartment anche per chi già esiste
        for (EventParticipant participant : currentParticipants) {
            if (!userIdsToRemove.contains(participant.getUser().getId())) {
                participant.setInvitedDirectly(
                        dto.userIds() != null && dto.userIds().contains(participant.getUser().getId())
                );
                participant.setInvitedThroughDepartment(!newDepartmentIds.isEmpty());
            }
        }

        eventRepository.save(event);
        eventParticipantRepository.saveAll(
                currentParticipants.stream()
                        .filter(p -> !userIdsToRemove.contains(p.getUser().getId()))
                        .toList()
        );

        return new EventResponseDTO(
                event.getId(),
                event.getName(),
                event.getLocation(),
                event.getDescription(),
                event.getStartAt(),
                event.getEndAt(),
                event.getType(),
                event.getAudienceType(),
                event.getCreatedBy().getId()
        );
    }
}
