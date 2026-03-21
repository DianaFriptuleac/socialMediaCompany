package dianafriptuleac.socialMediaCompany.repositories.Events;

import dianafriptuleac.socialMediaCompany.entities.Events.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

    //senza @EntityGraph: fai una query per gli event + poi UNA query per ogni creator
    //con @EntityGraph: tutto in una query sola (quando esegui questa query, fai join e carica anche createdBy subito)

    //@EntityGraph(attributePaths = {"createdBy"})
    Page<Event> findAllByOrderByStartAtDesc(Pageable pageable);

}
