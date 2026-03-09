package dianafriptuleac.socialMediaCompany.repositories.messages;

import dianafriptuleac.socialMediaCompany.entities.messages.MessageState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageStateRepository extends JpaRepository<MessageState, UUID> {
    Optional<MessageState> findByMessageIdAndUserId(UUID messageId, UUID userId);

    @Query("""
                select ms from MessageState ms
                where ms.message.id in :messageIds and ms.user.id = :userId
            """)
    List<MessageState> findAllByMessageIdsAndUserId(List<UUID> messageIds, UUID userId);

    // count unread in una conversazione
    @Query("""
                select count(ms)
                from MessageState ms
                where ms.user.id = :userId
                  and ms.message.conversation.id = :conversationId
                  and ms.deletedAt is null
                  and ms.readAt is null
                  and ms.message.sender.id <> :userId
            """)
    long countUnreadByConversationIdAndUserId(UUID conversationId, UUID userId);

    @Query("""
                select count(ms)
                from MessageState ms
                where ms.user.id = :userId
                  and ms.message.conversation.id = :conversationId
                  and ms.deletedAt is null
                  and ms.readAt is null
                  and ms.message.sender.id <> :userId
                  and ms.message.createdAt > :clearedAt
            """)
    long countUnreadByConversationIdAndUserIdAfterClear(UUID conversationId, UUID userId, Instant clearedAt);
}
