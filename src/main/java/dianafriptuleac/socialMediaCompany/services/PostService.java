package dianafriptuleac.socialMediaCompany.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import dianafriptuleac.socialMediaCompany.entities.Post;
import dianafriptuleac.socialMediaCompany.entities.PostMedia;
import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.enums.MediaType;
import dianafriptuleac.socialMediaCompany.exceptions.BadRequestException;
import dianafriptuleac.socialMediaCompany.payloads.Comment.UserPublicDTO;
import dianafriptuleac.socialMediaCompany.payloads.Post.PostCreateDTO;
import dianafriptuleac.socialMediaCompany.payloads.Post.PostMediaDTO;
import dianafriptuleac.socialMediaCompany.payloads.Post.PostResponseDTO;
import dianafriptuleac.socialMediaCompany.repositories.CommentRepository;
import dianafriptuleac.socialMediaCompany.repositories.PostLikeRepository;
import dianafriptuleac.socialMediaCompany.repositories.PostRepository;
import dianafriptuleac.socialMediaCompany.repositories.PostShareRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private PostLikeRepository postLikeRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private PostShareRepository postShareRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private Cloudinary cloudinary;

    // ---------------------- Create post (multipart: dto + files)

    @Transactional
    public PostResponseDTO createPost(User currentUser, PostCreateDTO body, List<MultipartFile> files) {
        if (body == null) throw new BadRequestException("Post body is required");

        Post post = new Post(currentUser, body.title(), body.description(), body.externalUrl());

        if (files != null) {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;

                String url = upload(file);
                MediaType type = detect(file);

                PostMedia media = new PostMedia(post, type, url);
                post.addMedia(media);
            }
        }

        Post saved = postRepository.save(post);
        return toResponse(saved);
    }

    // ----------------- helpers

    private String upload(MultipartFile file) {
        try (var is = file.getInputStream()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> res = cloudinary.uploader().upload(
                    is,
                    ObjectUtils.asMap(
                            "resource_type", "auto",
                            "folder", "posts"
                    )
            );

            Object url = res.get("secure_url"); // meglio di "url"
            if (url == null) url = res.get("url");
            if (url == null) throw new BadRequestException("Upload failed (missing url)");

            return url.toString();
        } catch (Exception e) {
            // IMPORTANTISSIMO: stampa l'errore reale
            System.out.println("Cloudinary upload error: " + e.getClass().getName() + " - " + e.getMessage());
            throw new BadRequestException("Error uploading media");
        }
    }


    private MediaType detect(MultipartFile file) {
        String ct = file.getContentType();
        if (ct == null) return MediaType.FILE;
        if (ct.startsWith("image/")) return MediaType.IMAGE;
        if (ct.startsWith("video/")) return MediaType.VIDEO;
        return MediaType.FILE;
    }

    private PostResponseDTO toResponse(Post post) {

        User a = post.getAuthor();
        UserPublicDTO author = new UserPublicDTO(
                a.getId(),
                a.getName(),
                a.getSurname(),
                a.getAvatar(),
                a.getRole()
        );

        List<PostMediaDTO> media = post.getMedia().stream()
                .map(m -> new PostMediaDTO(m.getId(), m.getType(), m.getUrl()))
                .toList();

        long likeCount = post.getLikes() != null ? post.getLikes().size() : 0;
        long commentCount = post.getComments() != null ? post.getComments().size() : 0;

        return new PostResponseDTO(
                post.getId(),
                author,
                post.getTitle(),
                post.getDescription(),
                post.getExternalUrl(),
                media,
                likeCount,
                commentCount,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}

