package dianafriptuleac.socialMediaCompany.services;

import dianafriptuleac.socialMediaCompany.entities.Department;
import dianafriptuleac.socialMediaCompany.entities.Events.Event;
import dianafriptuleac.socialMediaCompany.entities.Events.EventDepartment;
import dianafriptuleac.socialMediaCompany.entities.Events.EventParticipant;
import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.enums.Events.EventAudienceType;
import dianafriptuleac.socialMediaCompany.enums.Events.ParticipationStatus;
import dianafriptuleac.socialMediaCompany.exceptions.BadRequestException;
import dianafriptuleac.socialMediaCompany.exceptions.NotFoundException;
import dianafriptuleac.socialMediaCompany.payloads.Events.EventCreateDTO;
import dianafriptuleac.socialMediaCompany.repositories.DepartmentRepository;
import dianafriptuleac.socialMediaCompany.repositories.Events.EventDepartmentRepository;
import dianafriptuleac.socialMediaCompany.repositories.Events.EventParticipantRepository;
import dianafriptuleac.socialMediaCompany.repositories.Events.EventRepository;
import dianafriptuleac.socialMediaCompany.repositories.UserDepartmentRoleRepository;
import dianafriptuleac.socialMediaCompany.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                                dto.departmentIds() != null
                        )
                        .build())
                .toList();

        eventParticipantRepository.saveAll(participants);

        return savedEvent;
    }
}
