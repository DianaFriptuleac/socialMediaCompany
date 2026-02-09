package dianafriptuleac.socialMediaCompany.repositories.messages;

import dianafriptuleac.socialMediaCompany.entities.messages.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    //prende solo messaggi della conversazione
    //per l’utente corrente, esclude quelli cancellati “per me”
    //esclude quelli precedenti a clearedAt

    @Query("""
                select m
                from Message m
                join ConversationParticipant cp
                     on cp.conversation.id = m.conversation.id and cp.user.id = :userId
                left join MessageState ms
                     on ms.message.id = m.id and ms.user.id = :userId
                where m.conversation.id = :conversationId
                  and (cp.clearedAt is null or m.createdAt > cp.clearedAt)
                  and (ms.deletedAt is null)
                order by m.createdAt asc
            """)
    List<Message> findVisibleMessages(UUID conversationId, UUID userId, Pageable pageable);
}
