package dianafriptuleac.socialMediaCompany.controllers;

import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.payloads.Post.PostCreateDTO;
import dianafriptuleac.socialMediaCompany.payloads.Post.PostResponseDTO;
import dianafriptuleac.socialMediaCompany.services.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /*  public PostController(PostService postService) {
          this.postService = postService;
      }

      @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
      @ResponseStatus(HttpStatus.CREATED)
      public PostResponseDTO createPost(
              @AuthenticationPrincipal User currentUser,
              @RequestPart("post") PostCreateDTO body,
              @RequestPart(value = "files", required = false) List<MultipartFile> files
      ) {
          return postService.createPost(currentUser, body, files);
      }

      // SOLO JSON
      @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
      @ResponseStatus(HttpStatus.CREATED)
      public PostResponseDTO createPostJsonOnly(
              @AuthenticationPrincipal User currentUser,
              @RequestBody PostCreateDTO body
      ) {
          return postService.createPost(currentUser, body, null);
      }

     */
    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponseDTO createPost(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String externalUrl,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        PostCreateDTO body = new PostCreateDTO(title, description, externalUrl);
        return postService.createPost(currentUser, body, files);
    }

}