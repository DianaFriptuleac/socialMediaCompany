package dianafriptuleac.socialMediaCompany.entities.messages;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "message_attachments")
@NoArgsConstructor
@Getter
@Setter
public class MessageAttachment {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id")
    private Message message;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private long size;

    // Cloudinary
    @Column(nullable = false, unique = true)
    private String publicId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String secureUrl;

    @CreationTimestamp
    private Instant createdAt;
}
