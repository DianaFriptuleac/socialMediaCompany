package dianafriptuleac.socialMediaCompany.controllers;

import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.payloads.Comment.CommentCreateDTO;
import dianafriptuleac.socialMediaCompany.payloads.Comment.CommentResponseDTO;
import dianafriptuleac.socialMediaCompany.services.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDTO create(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID postId,
            @RequestBody @Valid CommentCreateDTO body
    ) {
        return commentService.create(currentUser, postId, body);
    }

    // lista commenti di un pos (root + replies)
    @GetMapping("/posts/{postId}/comments")
    public List<CommentResponseDTO> list(@PathVariable UUID postId) {
        return commentService.getByPost(postId);
    }

    // delete commento
    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID commentId
    ) {
        commentService.delete(currentUser, commentId);
    }
}
