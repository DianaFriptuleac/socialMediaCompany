package dianafriptuleac.socialMediaCompany.entities.jobs;

import dianafriptuleac.socialMediaCompany.entities.Department;
import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.enums.jobs.EmploymentType;
import dianafriptuleac.socialMediaCompany.enums.jobs.JobStatus;
import dianafriptuleac.socialMediaCompany.enums.jobs.WorkMode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_openings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobOpening {

    @Id
    @GeneratedValue
    private UUID id;

    private String title;
    private String desctription;
    private String requirements;
    private String location;

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;

    @Enumerated(EnumType.STRING)
    private WorkMode workMode;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    private LocalDate applicationDeadline;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    private LocalDateTime createdAt;
    

}
