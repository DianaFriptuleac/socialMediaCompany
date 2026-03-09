package dianafriptuleac.socialMediaCompany.repositories.messages;

import dianafriptuleac.socialMediaCompany.entities.messages.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

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

    @Query("""
                select m
                from Message m
                where m.conversation.id = :conversationId
                  and exists (
                      select ms.id
                      from MessageState ms
                      where ms.message = m
                        and ms.user.id = :userId
                        and ms.deletedAt is null
                  )
                order by m.createdAt desc
            """)
    List<Message> findLastVisibleMessages(UUID conversationId, UUID userId, Pageable pageable);

    @Query("""
                select m
                from Message m
                where m.conversation.id = :conversationId
                  and exists (
                      select 1
                      from MessageState ms
                      where ms.message = m
                        and ms.user.id = :userId
                        and ms.deletedAt is null
                  )
                  and m.createdAt > :clearedAt
                order by m.createdAt desc
            """)
    List<Message> findLastVisibleMessagesAfterClear(UUID conversationId, UUID userId, Instant clearedAt, Pageable pageable);
}