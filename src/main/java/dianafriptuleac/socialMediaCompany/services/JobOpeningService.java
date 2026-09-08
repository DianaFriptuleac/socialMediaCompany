package dianafriptuleac.socialMediaCompany.services;

import dianafriptuleac.socialMediaCompany.entities.Department;
import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.entities.jobs.JobOpening;
import dianafriptuleac.socialMediaCompany.enums.jobs.JobStatus;
import dianafriptuleac.socialMediaCompany.exceptions.BadRequestException;
import dianafriptuleac.socialMediaCompany.exceptions.NotFoundException;
import dianafriptuleac.socialMediaCompany.payloads.jobs.JobCreateDTO;
import dianafriptuleac.socialMediaCompany.payloads.jobs.JobUpdateDTO;
import dianafriptuleac.socialMediaCompany.repositories.DepartmentRepository;
import dianafriptuleac.socialMediaCompany.repositories.jobs.JobOpeningRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class JobOpeningService {

    @Autowired
    private JobOpeningRepository jobOpeningRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    @Lazy
    private JobApplicationService jobApplicationService;

    //---------- Create job
    public JobOpening createJob(JobCreateDTO dto, User creator) {

        // Crea dipartimento scelto
        Department department = departmentRepository.findById(dto.departmentId())
                .orElseThrow(() -> new NotFoundException("Department not found!"));
        // Controlla che la scadenza non sia gia passata
        if (dto.applicationDeadline() != null && dto.applicationDeadline().isBefore(LocalDate.now())) {
            throw new BadRequestException("Application deadline cannot be in the past");
        }

        // Crea nuova posizione
        JobOpening job = JobOpening.builder()
                .title(dto.title().trim())
                .description(dto.description().trim())
                .requirements(dto.requirements())
                .location(dto.location())
                .employmentType(dto.employmentType())
                .workMode(dto.workMode())
                .status(JobStatus.OPEN)
                .applicationDeadline(dto.applicationDeadline())
                .department(department)
                .createdBy(creator)
                .createdAt(LocalDateTime.now())
                .build();

        return jobOpeningRepository.save(job);
    }

    //------------- get open jobs
    public Page<JobOpening> getOpenJobs(Pageable pageable) {
        return jobOpeningRepository.findByStatus(
                JobStatus.OPEN, pageable
        );
    }


    //------------- get job by id
    public JobOpening getJobById(UUID jobId) {
        return jobOpeningRepository
                .findById(jobId)
                .orElseThrow(
                        () -> new NotFoundException("Job opening not found")
                );
    }

    //------------- update job
    @Transactional
    public JobOpening updateJob(UUID jobId, JobUpdateDTO dto) {
        JobOpening job = getJobById(jobId);

        // Non modificare se la posizione e gia chiusa
        if (job.getStatus() == JobStatus.CLOSED) {
            throw new BadRequestException("Closed jobs cannot be modified.");
        }
        if (dto.title() != null && !dto.title().isBlank()) {
            job.setTitle(dto.title().trim());
        }
        if (dto.description() != null && !dto.description().isBlank()) {
            job.setDescription(dto.description().trim());
        }
        if (dto.requirements() != null) {
            job.setRequirements(dto.requirements());
        }
        if (dto.location() != null) {
            job.setLocation(dto.location());
        }
        if (dto.employmentType() != null) {
            job.setEmploymentType(dto.employmentType());
        }
        if (dto.workMode() != null) {
            job.setWorkMode(dto.workMode());
        }
        if (dto.departmentId() != null) {
            Department department = departmentRepository.findById(dto.departmentId())
                    .orElseThrow(() -> new NotFoundException("Department not found"));
            job.setDepartment(department);
        }
        if (dto.applicationDeadline() != null) {
            if (dto.applicationDeadline().isBefore(LocalDate.now())) {
                throw new BadRequestException("Application deadline cannot be in the past");
            }
            job.setApplicationDeadline(dto.applicationDeadline());
        }
        return jobOpeningRepository.save(job);
    }

    //----------- Close Job
    @Transactional
    public JobOpening closeJob(UUID jobId) {
        JobOpening job = getJobById(jobId);

        // se e gia chiuso
        if (job.getStatus() == JobStatus.CLOSED) {
            throw new BadRequestException("This job is already closed");
        }
        job.setStatus(JobStatus.CLOSED);
        // salvo
        JobOpening savedJob = jobOpeningRepository.save(job);

        // notifica per i candidati
        jobApplicationService.notifyApplicantsJobClosed(savedJob);
        return savedJob;
    }


    // Delete job
    @Transactional
    public void deleteJob(UUID jobId) {
        JobOpening job = getJobById(jobId);
        // notifica
        jobApplicationService.notifyApplicantsJobDeleted(job);

        jobApplicationService.deleteApplicationsForJob(job);

        jobOpeningRepository.delete(job);
    }

}
