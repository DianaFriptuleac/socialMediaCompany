package dianafriptuleac.socialMediaCompany.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "post_shares")
@Getter
@Setter
@NoArgsConstructor
public class PostShare {

    @Id
    @GeneratedValue
    @Setter(AccessLevel.NONE)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_by_id")
    private User sharedBy;

    @ManyToMany
    @JoinTable(
            name = "post_share_recipients",
            joinColumns = @JoinColumn(name = "share_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> recipients = new HashSet<>();

    // opzionale
    @Column(columnDefinition = "TEXT")
    private String message;

    @CreationTimestamp
    private Instant createdAt;

    public PostShare(Post post, User sharedBy, Set<User> recipients, String message) {
        this.post = post;
        this.sharedBy = sharedBy;
        this.recipients = recipients;
        this.message = message;
    }
}
