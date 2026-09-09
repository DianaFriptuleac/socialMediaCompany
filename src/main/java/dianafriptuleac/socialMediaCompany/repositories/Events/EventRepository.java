package dianafriptuleac.socialMediaCompany.repositories.Events;

import dianafriptuleac.socialMediaCompany.entities.Events.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

    //senza @EntityGraph: fai una query per gli event + poi UNA query per ogni creator
    //con @EntityGraph: tutto in una query sola (quando esegui questa query, fai join e carica anche createdBy subito)

    //@EntityGraph(attributePaths = {"createdBy"})
    Page<Event> findAllByOrderByStartAtDesc(Pageable pageable);

    Page<Event> findByEndAtGreaterThanEqualOrderByStartAtAsc(
            LocalDateTime now,
            Pageable pageable
    );

    // search event
    @Query("""
            SELECT e
            FROM Event e
            WHERE e.endAt >= :now
              AND (
                  LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%'))
                  OR LOWER(e.location) LIKE LOWER(CONCAT('%', :query, '%'))
                  OR LOWER(e.description) LIKE LOWER(CONCAT('%', :query, '%'))
              )
            ORDER BY e.startAt ASC
            """)
    Page<Event> searchAvailableEvents(@Param("query") String query, @Param("now")
    LocalDateTime now, Pageable pageable);

}
