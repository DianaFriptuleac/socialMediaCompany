package dianafriptuleac.socialMediaCompany.entities.Events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.enums.Events.EventAudienceType;
import dianafriptuleac.socialMediaCompany.enums.Events.EventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String location;
    private String description;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    private EventType type;
    @Enumerated(EnumType.STRING)
    private EventAudienceType audienceType;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false,
            foreignKey = @ForeignKey(name = "fk_event_created_by"))
    private User createdBy;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<EventParticipant> participants = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<EventDepartment> targetDepartments = new HashSet<>();


}

