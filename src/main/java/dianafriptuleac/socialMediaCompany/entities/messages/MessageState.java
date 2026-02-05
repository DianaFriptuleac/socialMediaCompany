package dianafriptuleac.socialMediaCompany.entities.messages;

import dianafriptuleac.socialMediaCompany.entities.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "message_states",
        uniqueConstraints = @UniqueConstraint(columnNames = {"message_id", "user_id"})
        /* Per uno stesso messaggio, uno stesso utente può avere UNA SOLA riga di stato
        (un stato per utente per messaggio, Permette la cancellazione “solo per me”, read/unread affidabile)
         */
)
@Getter
@Setter
@NoArgsConstructor
public class MessageState {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    /* Relazione obbligatoria (NON può essere null - (FK NOT NULL)) e caricata in modo LAZY:
    l'entità collegata viene caricata dal DB solo quando viene effettivamente utilizzata.
    */
    @JoinColumn(name = "message_id")
    private Message message;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Instant readAt;  // un istante preciso, espresso come numero di secondi
    private Instant deletedAt;  // quando l'utente lo cancella “per me”

    public MessageState(Message message, User user) {
        this.message = message;
        this.user = user;
    }
}
