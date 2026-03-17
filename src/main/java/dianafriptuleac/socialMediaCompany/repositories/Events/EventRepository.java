package dianafriptuleac.socialMediaCompany.repositories.Events;

import dianafriptuleac.socialMediaCompany.entities.Events.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
}
