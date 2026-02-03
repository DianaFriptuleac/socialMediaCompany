package dianafriptuleac.socialMediaCompany.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.payloads.Post.PostCreateDTO;
import dianafriptuleac.socialMediaCompany.payloads.Post.PostResponseDTO;
import dianafriptuleac.socialMediaCompany.payloads.Post.PostUpdateDTO;
import dianafriptuleac.socialMediaCompany.services.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    // // Oggetto di Jackson usato per convertire JSON <-> oggetti Java (DTO)
    private final ObjectMapper objectMapper;

    // Costruttore: Spring fa dependency injection qui (consigliato rispetto ad @Autowired sui campi)
    public PostController(PostService postService, ObjectMapper objectMapper) {
        this.postService = postService;     // salva il service dentro la classs
        this.objectMapper = objectMapper;   // salva ObjectMapper dentro la classe
    }

    // Endpoint POST su /posts/with-media
    // "consumes multipart/form-data" : questa rotta accetta form-data con file (upload)
    @PostMapping(value = "/with-media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponseDTO createPostWithMedia(
            @AuthenticationPrincipal User currentUser,

            // Legge dal multipart chiamata post.
            // Trattata come String: deve contenere JSON (es: {"title":"...","description":"..."})
            @RequestPart("post") String postJson,

            // Legge dal multipart "files" (può esserci oppure no)
            // Se non ci sono file, Spring passa null
            @RequestPart(value = "files", required = false) List<MultipartFile> files

            // "throws Exception" - eventuali eccezioni non gestite qui
            // possono propagarsi e verranno gestite dal exception handler / Spring
    ) throws Exception {
        PostCreateDTO body;  // DTO finale (i dati del post)
        try {
            // Converte la stringa JSON ricevuta nel multipart in un oggetto PostCreateDTO
            body = objectMapper.readValue(postJson, PostCreateDTO.class);

            // Se il JSON è scritto male (virgolette mancanti, campi errati ecc.)
            // Jackson lancia JsonProcessingException.
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON in 'post' part", e);
        }

        return postService.createPost(currentUser, body, files);
    }

    // --------------- update post
    @PatchMapping(value = "/{postId}/with-media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public PostResponseDTO updatePostWithMedia(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID postId,

            // JSON con title/description/externalUrl opzionali
            @RequestPart(value = "post", required = false) String postJson,

            // lista id dei media da rimuovere (JSON array: ["uuid1","uuid2"])
            @RequestPart(value = "mediaToRemove", required = false) String mediaToRemoveJson,

            // nuovi file da aggiungere
            @RequestPart(value = "filesToAdd", required = false) List<MultipartFile> filesToAdd
    ) throws Exception {
        PostUpdateDTO body = null;
        if (postJson != null && !postJson.isBlank()) {
            try {
                body = objectMapper.readValue(postJson, PostUpdateDTO.class);
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                throw new IllegalArgumentException("Invalid JSON in 'post' part", e);
            }
        }
        List<UUID> mediaToRemove = List.of();

        if (mediaToRemoveJson != null && !mediaToRemoveJson.isBlank()) {
            try {
                mediaToRemove = objectMapper.readValue(
                        mediaToRemoveJson,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, UUID.class)
                );
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                throw new IllegalArgumentException("Invalid JSON in 'mediaToRemove' part", e);
            }
        }
        return postService.updatePost(currentUser, postId, body, mediaToRemove, filesToAdd);
    }

    //-----Delete Post
    @DeleteMapping("{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID postId
    ) {
        postService.deletePost(currentUser, postId);
    }
}