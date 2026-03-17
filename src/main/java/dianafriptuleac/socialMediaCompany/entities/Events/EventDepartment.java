package dianafriptuleac.socialMediaCompany.entities.Events;

import dianafriptuleac.socialMediaCompany.entities.Department;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "event_departments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_event_department", columnNames = {"event_id", "department_id"})
        })
//event e department possono comparire insieme una sola volta
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDepartment {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_event_department_event"))
    private Event event;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_event_department_department"))
    private Department department;
}
