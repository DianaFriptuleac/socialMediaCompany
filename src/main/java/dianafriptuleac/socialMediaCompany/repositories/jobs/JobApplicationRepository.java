package dianafriptuleac.socialMediaCompany.repositories.jobs;

import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.entities.jobs.JobApplication;
import dianafriptuleac.socialMediaCompany.entities.jobs.JobOpening;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {
    boolean existsByJobAndApplicant(
            JobOpening job, User applicant
    );

    Page<JobApplication> findByApplicant(User applicant, Pageable pageable);

    Page<JobApplication> findByJob(JobOpening job, Pageable pageable);

    long countByJob(JobOpening job);

    void deleteByJob(JobOpening job);
}
