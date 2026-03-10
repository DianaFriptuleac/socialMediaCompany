package dianafriptuleac.socialMediaCompany.services;

import com.cloudinary.Cloudinary;
import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.entities.messages.*;
import dianafriptuleac.socialMediaCompany.exceptions.BadRequestException;
import dianafriptuleac.socialMediaCompany.exceptions.NotFoundException;
import dianafriptuleac.socialMediaCompany.payloads.messages.*;
import dianafriptuleac.socialMediaCompany.repositories.UserRepository;
import dianafriptuleac.socialMediaCompany.repositories.messages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    @Autowired
    MessageAttachmentRepository messageAttachmentRepository;
    @Autowired
    private Cloudinary cloudinary;

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


    /// Lista delle conversazioni dell'utente, includendo:
    /// utente con cui sta parlando, ultimo msg, data ultimo msg, nr. msg non letti,
    /// ordinamento per conversazione più recente

    @Transactional(readOnly = true)
    public List<ConversationListItemDTO> listMyConversations(UUID myId) {
        // Recupera tutte le partecipazioni dell'utente alle conversazioni
        // (tutte le righe della tabella conversation_participant dove user_id = myId)
        List<ConversationParticipant> myParticipations = conversationParticipantRepository.findAllByUserId(myId);

        // Converte ogni partecipazione in un DTO per la lista conversazioni
        return myParticipations.stream()
                .map(cp -> {
                    // Recupera la conversazione associata alla partecipazione
                    Conversation conversation = cp.getConversation();

                    // Recupera tutti i partecipanti della conversazione
                    List<ConversationParticipant> participants =
                            conversationParticipantRepository.findAllByConversationId(conversation.getId());

                    // Cerca il partecipante che NON è l'utente corrente
                    // (per ottenere l'altro utente nella chat privata)
                    ConversationParticipant otherParticipant = participants.stream()
                            .filter(p -> !p.getUser().getId().equals(myId)) // esclude me
                            .findFirst() // prende il primo risultato
                            .orElse(null);  // se non esiste - ritorna null

                    // Costruisce il DTO dell'altro utente della conversazione
                    ConversationUserDTO otherUser = otherParticipant != null
                            ? new ConversationUserDTO(
                            otherParticipant.getUser().getId(),
                            otherParticipant.getUser().getName(),
                            otherParticipant.getUser().getSurname(),
                            otherParticipant.getUser().getAvatar()
                    )
                            : null;

                    // Variabile che conterrà l'ultimo messaggio visibile
                    List<Message> lastMessages;

                    // Se la conversazione è stata "clearata" dall'utente
                    if (cp.getClearedAt() != null) {

                        // Recupera l'ultimo messaggio DOPO il momento di clear
                        lastMessages = messageRepository.findLastVisibleMessagesAfterClear(
                                conversation.getId(),  // id conversazione
                                myId,  // utente corrente
                                cp.getClearedAt(),  // timestamp di clear
                                PageRequest.of(0, 1)  // prende solo 1 messaggio
                        );
                    } else {
                        // Recupera l'ultimo messaggio visibile della conversazione
                        lastMessages = messageRepository.findLastVisibleMessages(
                                conversation.getId(),
                                myId,
                                PageRequest.of(0, 1)
                        );
                    }

                    // Se la lista è vuota non esiste nessun messaggio
                    Message lastMessage = lastMessages.isEmpty() ? null : lastMessages.get(0);

                    // Variabile per il conteggio dei messaggi non letti
                    long unreadCount;
                    if (cp.getClearedAt() != null) {
                        // Conta solo i messaggi non letti DOPO il clear
                        unreadCount = messageStateRepository.countUnreadByConversationIdAndUserIdAfterClear(
                                conversation.getId(),
                                myId,
                                cp.getClearedAt()
                        );
                    } else {
                        // Conta tutti i messaggi non letti della conversazione
                        unreadCount = messageStateRepository.countUnreadByConversationIdAndUserId(
                                conversation.getId(),
                                myId
                        );
                    }

                    //DTO finale della conversazione
                    return new ConversationListItemDTO(
                            conversation.getId(),  // id conversazione
                            conversation.getCreatedAt(),  // data creazione conversazione
                            otherUser,  // utente con cui sto parlando
                            lastMessage != null ? lastMessage.getText() : null,  // testo ultimo msg
                            lastMessage != null ? lastMessage.getSender().getId() : null,  // id mittente ultimo msg
                            lastMessage != null ? lastMessage.getCreatedAt() : null,  // data ultimo msg
                            unreadCount  // nr. msg. non letti
                    );
                })
                // Ordina le conversazioni per data dell'ultimo messaggio (decrescente)
                .sorted((a, b) -> {
                    // Se non esiste un messaggio usa la data di creazione della conversazione
                    Instant aTime = a.lastMessageCreatedAt() != null ? a.lastMessageCreatedAt() : a.createdAt();
                    Instant bTime = b.lastMessageCreatedAt() != null ? b.lastMessageCreatedAt() : b.createdAt();
                    // Ordina dal più recente al più vecchio
                    return bTime.compareTo(aTime);
                })
                .toList();   // Converte lo stream in lista
    }


    ///Restituisce il dettaglio di una singola conversazione, includendo:
    /// id conversazione, data creazione, lista partecipanti
    @Transactional(readOnly = true)
    public ConversationDetailDTO getConversationDetail(UUID conversationId, UUID myId) {

        // Verifica che l'utente sia un partecipante della conversazione
        if (!conversationParticipantRepository.existsByConversationIdAndUserId(conversationId, myId)) {
            throw new IllegalArgumentException("Not a participant");
        }

        // Recupera la conversazione dal database
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        // Recupera tutti i partecipanti della conversazione
        List<ConversationUserDTO> participants = conversationParticipantRepository
                .findAllByConversationId(conversationId)
                // Converte ogni partecipante in DTO
                .stream()
                .map(cp -> new ConversationUserDTO(
                        cp.getUser().getId(),
                        cp.getUser().getName(),
                        cp.getUser().getSurname(),
                        cp.getUser().getAvatar()
                ))
                .toList();

        // Costruisce il DTO di dettaglio conversazione
        return new ConversationDetailDTO(
                conversation.getId(),
                conversation.getCreatedAt(),
                participants
        );
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
                Instant.now(), // readAt per me (sender),
                List.of()
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
                                : null,
                        List.of()
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

    //-------------------------------------- Attachments -------------------------------------------------

    @Transactional
    public AttachmentResponseDTO uploadAttachment(UUID myId, UUID messageId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Empty file");
        }

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        UUID conversationId = message.getConversation().getId();

        // solo i partecipanti possono allegare file
        if (!conversationParticipantRepository.existsByConversationIdAndUserId(conversationId, myId)) {
            throw new IllegalArgumentException("Not a participant");
        }
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of(
                            "folder", "chat_attachments",
                            "resource_type", "auto"
                    )
            );
            String publicId = (String) uploadResult.get("public_id");
            String secureUrl = (String) uploadResult.get("secure_url");

            MessageAttachment att = new MessageAttachment();
            att.setMessage(message);
            att.setFileName(fileName);
            att.setContentType(contentType);
            att.setSize(file.getSize());
            att.setPublicId(publicId);
            att.setSecureUrl(secureUrl);

            messageAttachmentRepository.save(att);

            return new AttachmentResponseDTO(
                    att.getId(),
                    att.getFileName(),
                    att.getContentType(),
                    att.getSize(),
                    att.getSecureUrl(),
                    att.getPublicId(),
                    att.getCreatedAt()
            );
        } catch (Exception e) {
            throw new RuntimeException("Cloudinary upload failed", e);
        }

    }

    @Transactional(readOnly = true)
    public List<AttachmentResponseDTO> listAttachmentsForMessage(UUID myId, UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        UUID conversationId = message.getConversation().getId();
        if (!conversationParticipantRepository.existsByConversationIdAndUserId(conversationId, myId)) {
            throw new IllegalArgumentException("Not a participant");
        }

        // 1) se ho cancellato quel messaggio "per me", non devo vedere allegati
        MessageState ms = messageStateRepository.findByMessageIdAndUserId(messageId, myId)
                .orElseThrow(() -> new NotFoundException("State not found"));
        if (ms.getDeletedAt() != null) {
            throw new NotFoundException("Message not visible");
        }

        // 2) se ho fatto clear e il messaggio è precedente, non devo vedere allegati
        ConversationParticipant cp = conversationParticipantRepository
                .findByConversationIdAndUserId(conversationId, myId)
                .orElseThrow(() -> new IllegalArgumentException("Not a participant"));

        if (cp.getClearedAt() != null && message.getCreatedAt().isBefore(cp.getClearedAt())) {
            throw new NotFoundException("Message not visible");
        }

        return messageAttachmentRepository.findAllByMessageId(messageId).stream()
                .map(att -> new AttachmentResponseDTO(
                        att.getId(),
                        att.getFileName(),
                        att.getContentType(),
                        att.getSize(),
                        att.getSecureUrl(),
                        att.getPublicId(),
                        att.getCreatedAt()
                ))
                .toList();
    }

}
