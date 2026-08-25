package dianafriptuleac.socialMediaCompany.entities.jobs;

import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.enums.jobs.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_applications",
        // la combinazione job_id + user_id deve essere unica - non puoi candidarti 2 volte per la stessa posizione
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"job_id", "user_id"}
                )
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor   // Crea automaticamente il costruttore con tutti i campi della classe
// creare oggetti impostando i campi in modo leggibile con .builder().campo(valore).build()
@Builder
public class JobApplication {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "job_id")
    private JobOpening job;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User applicant;

    private String cvUrl;
    private String cvPublicId;

    @Column(columnDefinition = "TEXT")
    private String coverLetterText;

    // URL della cover letter se caricata come file
    private String coverLetterUrl;

    // Public ID Cloudinary della cover letter
    private String coverLetterPublicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    private LocalDateTime appliedAt;

}
