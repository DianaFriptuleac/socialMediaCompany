package dianafriptuleac.socialMediaCompany.entities.Events;


import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.enums.Events.ParticipationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "event_participants",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_event_user", columnNames = {"event_id", "user_id"})
        })  // un utente può comparire una sola volta per evento
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// pattern Builder di Lombok per creare oggetti in modo fluido
public class EventParticipant {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_event_participant_event"))
    private Event event;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_event_participant_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipationStatus status;

    @Column(nullable = false)  //obbligatoria
    private boolean invitedDirectly;

    @Column(nullable = false)
    private boolean invitedThroughDepartment;
    // True se invitato tramite reparto/dipartimento

    private LocalDateTime respondedAt;
}