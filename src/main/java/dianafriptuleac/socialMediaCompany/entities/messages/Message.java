package dianafriptuleac.socialMediaCompany.entities.messages;

import dianafriptuleac.socialMediaCompany.entities.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
public class Message {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;


    // reply ad un messaggio precedente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    private Message replyTo;

    @CreationTimestamp
    private Instant createdAt;

    public Message(Conversation conversation, User sender, String text, Message replyTo) {
        this.conversation = conversation;
        this.sender = sender;
        this.text = text;
        this.replyTo = replyTo;
    }
}
