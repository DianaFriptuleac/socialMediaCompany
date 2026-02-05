package dianafriptuleac.socialMediaCompany.controllers;

import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.payloads.Post.PostLikeStatusDTO;
import dianafriptuleac.socialMediaCompany.payloads.Post.PostLikeUsersDTO;
import dianafriptuleac.socialMediaCompany.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/posts/{postId}/likes")
public class PostLikeController {

    // Give likes
    @Autowired
    private PostService postService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostLikeStatusDTO like(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID postId
    ) {
        return postService.likePost(currentUser, postId);
    }

    // Delete likes
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public PostLikeStatusDTO unlike(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID postId
    ) {
        return postService.unlikePost(currentUser, postId);
    }

    // Stato
    @GetMapping("/status")
    @ResponseStatus(HttpStatus.OK)
    public PostLikeStatusDTO status(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID postId
    ) {
        return postService.getLikeStatus(currentUser, postId);
    }

    // users
    @GetMapping("/users")
    public PostLikeUsersDTO users(@PathVariable UUID postId) {
        return postService.getLikeUsers(postId);
    }
}
