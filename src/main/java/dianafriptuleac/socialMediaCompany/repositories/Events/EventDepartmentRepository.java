package dianafriptuleac.socialMediaCompany.repositories.Events;

import dianafriptuleac.socialMediaCompany.entities.Events.EventDepartment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventDepartmentRepository extends JpaRepository<EventDepartment, UUID> {
    List<EventDepartment> findByEventId(UUID eventId);
}
