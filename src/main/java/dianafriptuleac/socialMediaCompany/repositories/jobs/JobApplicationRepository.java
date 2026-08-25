package dianafriptuleac.socialMediaCompany.repositories.jobs;

import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.entities.jobs.JobApplication;
import dianafriptuleac.socialMediaCompany.entities.jobs.JobOpening;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {
    boolean existsByJobAndApplicant(
            JobOpening job, User applicant
    );

    List<JobApplication> findByApplicant(User applicant);

    List<JobApplication> findByJob(JobOpening job);
}
