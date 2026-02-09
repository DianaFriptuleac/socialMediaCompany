package dianafriptuleac.socialMediaCompany.services;

import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.entities.messages.Conversation;
import dianafriptuleac.socialMediaCompany.entities.messages.ConversationParticipant;
import dianafriptuleac.socialMediaCompany.entities.messages.Message;
import dianafriptuleac.socialMediaCompany.entities.messages.MessageState;
import dianafriptuleac.socialMediaCompany.exceptions.NotFoundException;
import dianafriptuleac.socialMediaCompany.payloads.messages.ConversationResponseDTO;
import dianafriptuleac.socialMediaCompany.payloads.messages.MessageResponseDTO;
import dianafriptuleac.socialMediaCompany.payloads.messages.SendMessageRequestDTO;
import dianafriptuleac.socialMediaCompany.repositories.UserRepository;
import dianafriptuleac.socialMediaCompany.repositories.messages.ConversationParticipantRepository;
import dianafriptuleac.socialMediaCompany.repositories.messages.ConversationRepository;
import dianafriptuleac.socialMediaCompany.repositories.messages.MessageRepository;
import dianafriptuleac.socialMediaCompany.repositories.messages.MessageStateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    ConversationParticipantRepository conversationParticipantRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    MessageStateRepository messageStateRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ConversationRepository conversationRepository;

    //-------------------------------------- Conversation -------------------------------------------------
    @Transactional
    public ConversationResponseDTO createOrGetDirectConversation(UUID myId, UUID otherUserId) {
        User other = userRepository.findById(otherUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // se essiste gia, riusa
        List<UUID> existing = conversationParticipantRepository.findOneToOneConversationIds(myId, otherUserId);
        if (!existing.isEmpty()) {
            UUID conversationId = existing.get(0);
            Conversation c = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new NotFoundException("Conversation not found"));
            return new ConversationResponseDTO(c.getId(), List.of(myId, otherUserId),
                    c.getCreatedAt());
        }
        Conversation c = new Conversation();
        conversationRepository.save(c);

        conversationParticipantRepository.save(new ConversationParticipant(c, userRepository.getReferenceById(myId)));
        conversationParticipantRepository.save(new ConversationParticipant(c, other));

        return new ConversationResponseDTO(c.getId(), List.of(myId, otherUserId), c.getCreatedAt());

    }

    @Transactional
    public void clearConversationForMe(UUID conversationId, UUID myId) {
        ConversationParticipant cp = conversationParticipantRepository.findByConversationIdAndUserId(conversationId, myId)
                .orElseThrow(() -> new IllegalArgumentException("Not a participant"));
        cp.setClearedAt(Instant.now());
        conversationParticipantRepository.save(cp);
    }


    //-------------------------------------- Messages -------------------------------------------------
    @Transactional
    public MessageResponseDTO send(UUID myId, SendMessageRequestDTO requestDTO) {
        if (!conversationParticipantRepository.existsByConversationIdAndUserId(requestDTO.conversationId(), myId)) {
            throw new IllegalArgumentException("Not a participant");
        }
        Conversation conversation = new Conversation();
        conversation.setId(requestDTO.conversationId());

        Message replyTo = null;
        if (requestDTO.replyToMessageId() != null) {
            replyTo = messageRepository.findById(requestDTO.replyToMessageId())
                    .orElseThrow(() -> new NotFoundException("Reply nessage not found"));
        }
        Message m = new Message(conversation, userRepository.getReferenceById(myId), requestDTO.text(), replyTo);
        messageRepository.save(m);
        // crea stati per tutti i partecipanti (così la cancellazione “per me” è sempre possibile)
        List<ConversationParticipant> participants = conversationParticipantRepository
                .findAllByConversationId(requestDTO.conversationId());
        for (ConversationParticipant cp : participants) {
            MessageState ms = new MessageState(m, cp.getUser());
            if (cp.getUser().getId().equals(myId)) ms.setReadAt(Instant.now());  // il sender - gia "letto"
            messageStateRepository.save(ms);
        }
        return new MessageResponseDTO(
                m.getId(),
                requestDTO.conversationId(),
                myId,
                m.getText(),
                m.getReplyTo() != null ? m.getReplyTo().getId() : null,
                m.getCreatedAt(),
                Instant.now() // readAt per me (sender)
        );

    }

    @Transactional(readOnly = true)
    public List<MessageResponseDTO> listVisible(UUID myId, UUID conversationId, int page, int size) {
        List<Message> messages = messageRepository.findVisibleMessages(
                conversationId, myId, PageRequest.of(page, size)
        );
        List<UUID> messageIds = messages.stream().map(Message::getId).toList();
        List<MessageState> messageStates =
                messageStateRepository.findAllByMessageIdsAndUserId(messageIds, myId);

        Map<UUID, MessageState> stateByMessageId = messageStates.stream()
                .collect(Collectors.toMap(
                        ms -> ms.getMessage().getId(),
                        ms -> ms
                ));

        return messages.stream()
                .map(m -> new MessageResponseDTO(
                        m.getId(),
                        m.getConversation().getId(),
                        m.getSender().getId(),
                        m.getText(),
                        m.getReplyTo() != null ? m.getReplyTo().getId() : null,
                        m.getCreatedAt(),
                        stateByMessageId.containsKey(m.getId())
                                ? stateByMessageId.get(m.getId()).getReadAt()
                                : null
                ))
                .toList();
    }

    @Transactional
    public void deleteMessageForMe(UUID myId, UUID messageId) {
        MessageState ms = messageStateRepository.findByMessageIdAndUserId(messageId, myId)
                .orElseThrow(() -> new NotFoundException("State not found"));
        ms.setDeletedAt(Instant.now());
        messageStateRepository.save(ms);
    }

    @Transactional
    public void markAsRead(UUID myId, UUID messageId) {
        MessageState ms = messageStateRepository.findByMessageIdAndUserId(messageId, myId)
                .orElseThrow(() -> new NotFoundException("State not found"));
        if (ms.getReadAt() == null) {
            ms.setReadAt(Instant.now());
            messageStateRepository.save(ms);
        }
    }
}
