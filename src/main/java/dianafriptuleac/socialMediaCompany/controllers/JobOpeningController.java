package dianafriptuleac.socialMediaCompany.controllers;

import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.entities.jobs.JobOpening;
import dianafriptuleac.socialMediaCompany.payloads.jobs.JobCreateDTO;
import dianafriptuleac.socialMediaCompany.payloads.jobs.JobUpdateDTO;
import dianafriptuleac.socialMediaCompany.services.JobOpeningService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/jobs")
public class JobOpeningController {

    @Autowired
    private JobOpeningService jobOpeningService;

    @GetMapping
    public Page<JobOpening> getOpenJobs(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return jobOpeningService.getOpenJobs(pageable);
    }

    @GetMapping("/{jobId}")
    public JobOpening getJobById(@PathVariable UUID jobId) {
        return jobOpeningService.getJobById(jobId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public JobOpening createJob(@RequestBody @Valid JobCreateDTO dto,
                                @AuthenticationPrincipal User currentUser) {
        return jobOpeningService.createJob(dto, currentUser);
    }


    @PutMapping("/{jobId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public JobOpening updateJob(@PathVariable UUID jobId, @RequestBody @Valid JobUpdateDTO dto) {
        return jobOpeningService.updateJob(jobId, dto);
    }


    @PatchMapping("/{jobId}/close")
    @PreAuthorize("hasAuthority('ADMIN')")
    public JobOpening closeJob(@PathVariable UUID jobId) {
        return jobOpeningService.closeJob(jobId);
    }


    @DeleteMapping("/{jobId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJob(@PathVariable UUID jobId) {
        jobOpeningService.deleteJob(jobId);
    }
}
