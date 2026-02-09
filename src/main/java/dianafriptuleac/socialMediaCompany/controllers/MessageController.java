package dianafriptuleac.socialMediaCompany.controllers;

import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.payloads.messages.*;
import dianafriptuleac.socialMediaCompany.services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    // ---------------- Conversation ----------------

    @PostMapping("/conversations/direct")
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationResponseDTO createDirect(Authentication auth,
                                                @RequestBody CreateConversationRequestDTO req) {
        UUID myId = ((User) auth.getPrincipal()).getId();
        return messageService.createOrGetDirectConversation(myId, req.otherUserId());
    }

    @PostMapping("/conversations/{conversationId}/clear")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@PathVariable UUID conversationId, Authentication auth) {
        UUID myId = ((User) auth.getPrincipal()).getId();
        messageService.clearConversationForMe(conversationId, myId);
    }


    // ---------------- Messages ----------------
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponseDTO send(Authentication auth,
                                   @RequestBody SendMessageRequestDTO req) {
        UUID myId = ((User) auth.getPrincipal()).getId();
        return messageService.send(myId, req);
    }

    @GetMapping("/conversation/{conversationId}")
    @ResponseStatus(HttpStatus.OK)
    public List<MessageResponseDTO> list(@PathVariable UUID conversationId,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "50") int size,
                                         Authentication auth) {
        UUID myId = ((User) auth.getPrincipal()).getId();
        return messageService.listVisible(myId, conversationId, page, size);
    }

    @PostMapping("/{messageId}/read")
    @ResponseStatus(HttpStatus.OK)
    public void read(@PathVariable UUID messageId, Authentication auth) {
        UUID myId = ((User) auth.getPrincipal()).getId();
        messageService.markAsRead(myId, messageId);
    }

    @DeleteMapping("/{messageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteForMe(@PathVariable UUID messageId, Authentication auth) {
        UUID myId = ((User) auth.getPrincipal()).getId();
        messageService.deleteMessageForMe(myId, messageId);
    }

    // ---------------- Attachments ----------------
    // Upload allegato su un messaggio
    @PostMapping("/{messageId}/attachments")
    @ResponseStatus(HttpStatus.CREATED)
    public AttachmentResponseDTO upload(@PathVariable UUID messageId,
                                        @RequestParam("file") MultipartFile file,
                                        Authentication auth) {
        UUID myId = ((User) auth.getPrincipal()).getId();
        return messageService.uploadAttachment(myId, messageId, file);
    }

    // Lista allegati di un messaggio (solo se il messaggio è "visibile" per me)
    @GetMapping("/{messageId}/attachments")
    @ResponseStatus(HttpStatus.OK)
    public List<AttachmentResponseDTO> listByMessage(@PathVariable UUID messageId,
                                                     Authentication auth) {
        UUID myId = ((User) auth.getPrincipal()).getId();
        return messageService.listAttachmentsForMessage(myId, messageId);
    }

}

