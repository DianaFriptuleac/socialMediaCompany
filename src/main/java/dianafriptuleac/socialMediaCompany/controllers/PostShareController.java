package dianafriptuleac.socialMediaCompany.controllers;

import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.payloads.Post.PostShareResponseDTO;
import dianafriptuleac.socialMediaCompany.payloads.Post.SharePostDTO;
import dianafriptuleac.socialMediaCompany.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class PostShareController {

    @Autowired
    private PostService postService;

    @PostMapping("/posts/{postId}/share")
    @ResponseStatus(HttpStatus.CREATED)
    public void share(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID postId,
            @RequestBody SharePostDTO body
    ) {
        postService.sharePost(currentUser, postId, body);
    }

    @GetMapping("/me/inbox")
    public Page<PostShareResponseDTO> inbox(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return postService.inbox(currentUser, page, size);
    }
}
