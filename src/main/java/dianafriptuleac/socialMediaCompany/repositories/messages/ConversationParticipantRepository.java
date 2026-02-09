package dianafriptuleac.socialMediaCompany.repositories.messages;

import dianafriptuleac.socialMediaCompany.entities.messages.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, UUID> {
    Optional<ConversationParticipant> findByConversationIdAndUserId(UUID conversationId, UUID userId);

    boolean existsByConversationIdAndUserId(UUID conversationId, UUID userId);

    List<ConversationParticipant> findAllByConversationId(UUID conversationId);

    Optional<ConversationParticipant> findByUserIdAndConversationId(UUID userId, UUID conversationId);

    // per vedere se esiste già una chat 1–1 tra due utenti
    @Query("""
                select cp.conversation.id
                from ConversationParticipant cp
                where cp.user.id in (:u1, :u2)
                group by cp.conversation.id
                having count(cp.conversation.id) = 2
            """)
    List<UUID> findOneToOneConversationIds(UUID u1, UUID u2);
}
