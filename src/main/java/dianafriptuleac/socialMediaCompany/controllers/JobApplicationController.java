package dianafriptuleac.socialMediaCompany.controllers;

import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.entities.jobs.JobApplication;
import dianafriptuleac.socialMediaCompany.payloads.jobs.ApplicationStatusDTO;
import dianafriptuleac.socialMediaCompany.services.JobApplicationService;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("job_applications")
public class JobApplicationController {

    @Autowired
    private JobApplicationService jobApplicationService;

    // candidarsi
    @PostMapping(value = "/jobs/{jobId}/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public JobApplication applyToJob(
            @PathVariable UUID jobId,
            @AuthenticationPrincipal User currentUser,
            @RequestPart("cv") MultipartFile cv,
            @RequestPart(value = "coverLetterText",
                    required = false)
            String coverLetterText,
            @RequestPart(
                    value = "coverLetterFile",
                    required = false
            )
            MultipartFile coverLetterFile
    ) throws IOException {
        return jobApplicationService.applyToJob(jobId, currentUser, cv, coverLetterText, coverLetterFile);
    }

    // le mie candidature
    @GetMapping("/me")
    public Page<JobApplication> getMyApplications(
            @AuthenticationPrincipal User currentUser,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return jobApplicationService.getMyApplications(currentUser, pageable);
    }

    // ritira la candidatura
    @PatchMapping("/{applicationId}/withdraw")
    public JobApplication withdrawApplication(
            @PathVariable UUID applicationId,
            @AuthenticationPrincipal User currentUser
    ) throws BadRequestException {
        return jobApplicationService.withdrawApplication(applicationId, currentUser);
    }

    @GetMapping("/jobs/{jobId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<JobApplication> getApplicationsByJob(
            @PathVariable UUID jobId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return jobApplicationService.getApplicationsByJob(jobId, pageable);
    }

    @GetMapping("/jobs/{jobId}/count")
    @PreAuthorize("hasAuthority('ADMIN')")
    public long countApplicationsByJob(@PathVariable UUID jobId) {
        return jobApplicationService.countApplicationsByJob(jobId);
    }

    @PatchMapping("/{applicationId}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public JobApplication updateApplicationJobStatus(
            @PathVariable UUID applicationId,
            @RequestBody @Valid ApplicationStatusDTO dto
    ) throws BadRequestException {
        return jobApplicationService.updateApplicationStatus(applicationId, dto.status());
    }

}
