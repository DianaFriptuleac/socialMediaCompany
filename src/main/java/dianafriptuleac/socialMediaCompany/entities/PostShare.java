package dianafriptuleac.socialMediaCompany.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
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
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @Column(columnDefinition = "TEXT")
    private String message; // opzionale

    @CreationTimestamp
    private Instant createdAt;

    private Instant readAt; // (“letto/non letto”)

    public PostShare(Post post, User sender, User recipient, String message) {
        this.post = post;
        this.sender = sender;
        this.recipient = recipient;
        this.message = message;
    }
}
