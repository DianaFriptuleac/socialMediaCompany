package dianafriptuleac.socialMediaCompany.repositories.Events;

import dianafriptuleac.socialMediaCompany.entities.Events.EventParticipant;
import dianafriptuleac.socialMediaCompany.enums.Events.ParticipationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventParticipantRepository extends JpaRepository<EventParticipant, UUID> {
    List<EventParticipant> findByEventId(UUID eventId);

    List<EventParticipant> findByUserId(UUID userId);

    Optional<EventParticipant> findByEventIdAndUserId(UUID eventId, UUID userId);

    long countByEventId(UUID eventId);

    long countByEventIdAndStatus(UUID eventId, ParticipationStatus status);
}
