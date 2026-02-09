package dianafriptuleac.socialMediaCompany.repositories.messages;

import dianafriptuleac.socialMediaCompany.entities.messages.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
}
