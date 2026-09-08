package dianafriptuleac.socialMediaCompany.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.entities.jobs.JobApplication;
import dianafriptuleac.socialMediaCompany.entities.jobs.JobOpening;
import dianafriptuleac.socialMediaCompany.enums.jobs.ApplicationStatus;
import dianafriptuleac.socialMediaCompany.enums.jobs.JobStatus;
import dianafriptuleac.socialMediaCompany.exceptions.BadRequestException;
import dianafriptuleac.socialMediaCompany.exceptions.NotFoundException;
import dianafriptuleac.socialMediaCompany.repositories.jobs.JobApplicationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class JobApplicationService {

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private JobOpeningService jobOpeningService;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private NotificationService notificationService;

    // --------- Candidatura
    @Transactional
    public JobApplication applyToJob(
            UUID jobId, User applicant, MultipartFile cv, String coverLetterText, MultipartFile coverLetterFile)
            throws IOException {
        // cerca posizione
        JobOpening job = jobOpeningService.getJobById(jobId);

        // controllo lo stato posizione
        if (job.getStatus() != JobStatus.OPEN) {
            throw new BadRequestException("This job position is already closed.");
        }
        // controllo la scadenza
        if (job.getApplicationDeadline() != null && job.getApplicationDeadline().isBefore(LocalDate.now())) {
            throw new BadRequestException("The application deadline has expired");
        }
        // controllo che non abbia gia inviato la candidatura
        if (jobApplicationRepository.existsByJobAndApplicant(job, applicant)) {
            throw new BadRequestException("You have already applied for this position");
        }
        // CV obbligatorio
        if (cv == null || cv.isEmpty()) {
            throw new BadRequestException("CV is required.");
        }
        // controlla se il cv e pdf
        if (!"application/pdf".equalsIgnoreCase(cv.getContentType())) {
            throw new BadRequestException("CV must be a PDF file");
        }

        // upload cv
        // Carica il CV su Cloudinary e salva nella Map i dati restituiti dall'upload
        Map cvUpload = cloudinary.uploader().upload(
                // Converte il file MultipartFile ricevuto dal controller in un array di byt che Cloudinary può caricare
                cv.getBytes(),
                // Crea una Map con le opzioni da passare a Cloudinary
                ObjectUtils.asMap
                        // Dice a Cloudinary che il file è un documento/file generico e non un'immagine (pdf)
                                ("resource_type", "image",
                                        //cartella di Cloudinary dove salvare il CV
                                        "folder", "job_applications/cv"
                                )
        );
        // Recupera dalla risposta di Cloudinary l'URL HTTPS del CV appena caricato che
        // verrà salvato nel database per poter accedere/scaricare il CV
        String cvUrl = cvUpload.get("secure_url").toString();

        // Recupera l'identificativo univoco assegnato da Cloudinary al file,
        // per poter eliminare o sostituire il CV in futuro
        String cvPublicId = cvUpload.get("public_id").toString();

        // cover letter file
        String coverLetterUrl = null;
        String coverLetterPublicId = null;

        // file della cover letter facoltativo
        if (coverLetterFile != null && !coverLetterFile.isEmpty()) {
            // accetta pdf
            if (!"application/pdf".equalsIgnoreCase(
                    coverLetterFile.getContentType()
            )) {
                throw new BadRequestException("Cover letter file must be a PDF");
            }
            Map coverUpload = cloudinary.uploader().upload(
                    coverLetterFile.getBytes(), ObjectUtils.asMap(
                            "resource_type", "image",
                            "folder",
                            "job_applications/cover_letters"
                    )
            );

            coverLetterUrl = coverUpload.get("secure_url").toString();
            coverLetterPublicId = coverUpload.get("public_id").toString();
        }
        // crea candidatura
        JobApplication application = JobApplication.builder()
                .job(job)
                .applicant(applicant)
                .cvUrl(cvUrl)
                .cvPublicId(cvPublicId)
                .coverLetterText(
                        coverLetterText != null
                                && !coverLetterText.isBlank()
                                ? coverLetterText.trim()
                                : null
                )
                .coverLetterUrl(coverLetterUrl)
                .coverLetterPublicId(coverLetterPublicId)
                .status(ApplicationStatus.SUBMITTED)
                .appliedAt(LocalDateTime.now())
                .build();

        return jobApplicationRepository.save(application);
    }

    // --------------- get my application
    public Page<JobApplication> getMyApplications(
            User applicant, Pageable pageable
    ) {
        return jobApplicationRepository.findByApplicant(applicant, pageable);
    }

    // ---------------- get application by id
    public JobApplication getApplicationById(UUID applicationId) {
        return jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Job application not found."));
    }

    // --------------- get applications for job (ADMIN)
    public Page<JobApplication> getApplicationsByJob(
            UUID jobId,
            Pageable pageable
    ) {

        JobOpening job = jobOpeningService.getJobById(jobId);

        return jobApplicationRepository.findByJob(
                job,
                pageable
        );
    }

    // ----------- count applications for job (admin)
    public long countApplicationsByJob(UUID jobId) {
        JobOpening job = jobOpeningService.getJobById(jobId);
        return jobApplicationRepository.countByJob(job);
    }

    // ------------ change application status (admin)
    @Transactional
    public JobApplication updateApplicationStatus(UUID applicationId, ApplicationStatus newStatus) {
        // Cerca la candidatura
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Job application not found."));

        // un candidato ritirato non dovrebbe essere riattivato
        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            throw new BadRequestException("A withdrawn application cannot be updated");
        }
        // salvo il vecchio stato
        ApplicationStatus oldStatus = application.getStatus();
        // evito di creare una notifica se Admin seleziona lo stesso stato
        if (oldStatus == newStatus) {
            return application;
        }
        // cambio stato
        application.setStatus(newStatus);

        // salvo la candidatura aggiornata
        JobApplication savedApplication = jobApplicationRepository.save(application);

        // creo la notifica per candidato
        createApplicationStatusNotification(savedApplication, newStatus);
        return savedApplication;
    }

    // --------------- ritirare la candidatura
    @Transactional
    public JobApplication withdrawApplication(
            UUID applicationId,
            User currentUser
    ) {

        JobApplication application = jobApplicationRepository
                .findById(applicationId)
                .orElseThrow(
                        () -> new NotFoundException(
                                "Job application not found"
                        )
                );

        // Puo ritirare solo la propria candidatura
        if (!application.getApplicant().getId().equals(currentUser.getId())) {
            throw new BadRequestException(
                    "You cannot withdraw another user's application"
            );
        }
        // Se e gia stata ritirata
        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            throw new BadRequestException("Application is already withdrawn");
        }
        // impedire il ritiro se e gia stata accettata
        if (application.getStatus() == ApplicationStatus.ACCEPTED) {
            throw new BadRequestException("An accepted application cannot be withdrawn");
        }
        application.setStatus(
                ApplicationStatus.WITHDRAWN
        );
        return jobApplicationRepository.save(application);
    }

    // ---------------- delete all applications for a job
    @Transactional
    public void deleteApplicationsForJob(JobOpening job) {

        // candidature di quel job
        List<JobApplication> applications = jobApplicationRepository.findAllByJob(job);

        for (JobApplication application : applications) {
            try {
                //elimina cv da cloudinary
                if (application.getCvPublicId() != null) {
                    cloudinary.uploader().destroy(
                            application.getCvPublicId(),
                            ObjectUtils.asMap(
                                    "resource_type",
                                    "image"
                            )
                    );
                }
                // elimina cover letter da Cloudinary
                if (application.getCoverLetterPublicId() != null) {
                    cloudinary.uploader().destroy(
                            application.getCoverLetterPublicId(),
                            ObjectUtils.asMap(
                                    "resource_type",
                                    "image"
                            )
                    );
                }
            } catch (IOException e) {
                throw new RuntimeException("Error deleting application files", e);
            }
        }
        jobApplicationRepository.deleteAll(applications);
    }

    // --------create notification when application status changes
    private void createApplicationStatusNotification(
            JobApplication application, ApplicationStatus newStatus
    ) {
        String jobTitle = application.getJob().getTitle();
        String title;
        String message;

        switch (newStatus) {
            case UNDER_REVIEW -> {
                title = "Application under review";
                message = "Your application for " + jobTitle + " is now under review.";
            }
            case INTERVIEW -> {
                title = "Interview";
                message = "You have been selected for an interview for " + jobTitle + " .";
            }
            case ACCEPTED -> {
                title = "Application accepted";
                message = "Your application for " + jobTitle + " has been accepted.";
            }
            case REJECTED -> {
                title = "Application rejected";
                message = "Your application for " + jobTitle + " has been rejected.";
            }
            default -> {
                title = "Application updated";
                message = "The status of your application for " + jobTitle + " has changed.";
            }
        }
        notificationService.createNotification(application.getApplicant(), title, message, application.getJob().getId(),
                "JOB_APPLICATION_STATUS");
    }

    // ------------- notify applicants when job closes
    public void notifyApplicantsJobClosed(JobOpening job) {
        List<JobApplication> applications = jobApplicationRepository.findAllByJob(job);

        for (JobApplication application : applications) {
            // se la candidatura e stata ritirata - niente notifica
            if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
                continue;
            }
            notificationService.createNotification(application.getApplicant(),
                    "Job position closed",
                    "The position " + job.getTitle() + " is no longer available.",
                    job.getId(),
                    "JOB_CLOSED");
        }
    }

    // ------------- notify applicants when job deleted
    public void notifyApplicantsJobDeleted(JobOpening job) {
        List<JobApplication> applications = jobApplicationRepository.findAllByJob(job);

        for (JobApplication application : applications) {
            if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
                continue;
            }
            notificationService.createNotification(application.getApplicant(),
                    "Job position deleted",
                    "The position " + job.getTitle() + " has been removed.",
                    job.getId(),
                    "JOB_DELETED");
        }
    }
}
