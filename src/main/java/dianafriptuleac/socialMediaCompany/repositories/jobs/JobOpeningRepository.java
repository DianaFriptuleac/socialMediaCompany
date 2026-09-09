package dianafriptuleac.socialMediaCompany.repositories.jobs;

import dianafriptuleac.socialMediaCompany.entities.jobs.JobOpening;
import dianafriptuleac.socialMediaCompany.enums.jobs.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.UUID;

public interface JobOpeningRepository extends JpaRepository<JobOpening, UUID> {
    Page<JobOpening> findByStatus(
            JobStatus status, Pageable pageable
    );

    @Query("""
                SELECT j
                FROM JobOpening j
                WHERE j.status = :status
                AND (
                    j.applicationDeadline IS NULL
                    OR j.applicationDeadline >= :today
                )
            """)
    Page<JobOpening> findVisibleJobs(
            @Param("status") JobStatus status,
            @Param("today") LocalDate today,
            Pageable pageable
    );

    // Search jobs
    @Query("""
            SELECT j
            FROM JobOpening j
            LEFT JOIN j.department d
            WHERE j.status = :status
              AND (
                  j.applicationDeadline IS NULL
                  OR j.applicationDeadline >= :today
              )
              AND (
                  LOWER(j.title) LIKE LOWER(CONCAT('%', :query, '%'))
                  OR LOWER(j.description) LIKE LOWER(CONCAT('%', :query, '%'))
                  OR LOWER(j.requirements) LIKE LOWER(CONCAT('%', :query, '%'))
                  OR LOWER(j.location) LIKE LOWER(CONCAT('%', :query, '%'))
                  OR LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%'))
              )
            ORDER BY j.createdAt DESC
            """)
    Page<JobOpening> searchVisibleJobs(
            @Param("query") String query, @Param("status") JobStatus status,
            @Param("today") LocalDate today, Pageable pageable
    );
}
