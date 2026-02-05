package dianafriptuleac.socialMediaCompany.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import dianafriptuleac.socialMediaCompany.entities.*;
import dianafriptuleac.socialMediaCompany.enums.MediaType;
import dianafriptuleac.socialMediaCompany.enums.Role;
import dianafriptuleac.socialMediaCompany.exceptions.BadRequestException;
import dianafriptuleac.socialMediaCompany.exceptions.NotFoundException;
import dianafriptuleac.socialMediaCompany.exceptions.UnauthorizedException;
import dianafriptuleac.socialMediaCompany.payloads.Comment.UserPublicDTO;
import dianafriptuleac.socialMediaCompany.payloads.Post.*;
import dianafriptuleac.socialMediaCompany.repositories.CommentRepository;
import dianafriptuleac.socialMediaCompany.repositories.PostLikeRepository;
import dianafriptuleac.socialMediaCompany.repositories.PostRepository;
import dianafriptuleac.socialMediaCompany.repositories.PostShareRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    // ------------------------------- Helpers per creare Post ------------------------

    /**
     * Carica un file su Cloudinary e ritorna l'URL sicuro.
     * Usa file.getBytes() perché è accettato in modo affidabile dalla SDK.
     */
    private String upload(MultipartFile file) {
        try {
            // Upload su Cloudinary: ritorna una mappa con info (secure_url, public_id, ecc.)
            Map<String, Object> res = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "auto", // lascia decidere a Cloudinary se image/video/raw
                            "folder", "posts"                // salva dentro la cartella "posts"
                    )
            );

            //1. https
            Object url = res.get("secure_url"); // https

            //2. Se secure_url manca, prova "url" (http) come fallback
            if (url == null) url = res.get("url");

            //3. Se manca ancora - fallito
            if (url == null) throw new BadRequestException("Upload failed (missing url)");

            //4. Ritorna l'URL finale come stringa
            return url.toString();

        } catch (Exception e) {
            // stampa errore
            System.out.println("Cloudinary upload error: " + e.getClass().getName() + " - " + e.getMessage());
            throw new BadRequestException("Error uploading media");
        }
    }

    // -----------------------------------------

    /**
     * Decide il tipo di media basandosi sul content-type (MIME) del file.
     */
    private MediaType detect(MultipartFile file) {

        // Content-Type es: "image/png", "video/mp4", "application/pdf"
        String ct = file.getContentType();

        // Se manca il content-type - file generico
        if (ct == null) return MediaType.FILE;
        if (ct.startsWith("image/")) return MediaType.IMAGE;
        if (ct.startsWith("video/")) return MediaType.VIDEO;
        // Altrimenti file generico (pdf, zip, doc, ecc.)
        return MediaType.FILE;
    }

    // ------------------------------------------

    /**
     * Converte un Post entity in un PostResponseDTO + Costruisce l'author DTO e l'elenco media DTO.
     */
    private PostResponseDTO toResponse(Post post) {
        return toResponse(post, null);
    }

    private PostResponseDTO toResponse(Post post, User currentUser) {
        // Recupera l'autore dal post (entity User)
        User a = post.getAuthor();

        // Crea una versione "pubblica" dell'utente (senza dati sensibili)
        UserPublicDTO author = new UserPublicDTO(
                a.getId(),
                a.getName(),
                a.getSurname(),
                a.getAvatar(),
                a.getRole()
        );
        // Converte la Set<PostMedia> in List<PostMediaDTO> per la response
        List<PostMediaDTO> media = post.getMedia().stream()
                .map(m -> new PostMediaDTO(
                        m.getId(),   // id del media
                        m.getType(), // IMAGE/VIDEO/FILE
                        m.getUrl())) // url Cloudinary
                .toList();

        // NR. like: se la collection è null - 0
        long likeCount = postLikeRepository.countByPost_Id(post.getId());
        // NR. commenti
        long commentCount = commentRepository.countByPost_Id(post.getId());

        boolean likedByMe = currentUser != null &&
                postLikeRepository.findByPost_IdAndUser_Id(post.getId(), currentUser.getId()).isPresent();

        //DTO finale della response
        return new PostResponseDTO(
                post.getId(),
                author,
                post.getTitle(),
                post.getDescription(),
                post.getExternalUrl(),
                media,
                likeCount,
                commentCount,
                likedByMe,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }


    // ---------------------- Create post (multipart: dto + files) --------------

    /**
     * Crea un post con eventuali file allegati.
     *
     * @Transactional: se qualcosa fallisce (upload/DB) fa rollback delle operazioni DB
     * (annullare automaticamente tutte le modifiche fatte durante l’operazione)
     */
    @Transactional
    public PostResponseDTO createPost(User currentUser, PostCreateDTO body, List<MultipartFile> files) {
        if (body == null) throw new BadRequestException("Post body is required");

        // Crea l'entity Post a partire dai dati del DTO e dall'utente loggato (author)
        Post post = new Post(
                currentUser,
                body.title(),
                body.description(),
                body.externalUrl());

        // Se ci sono file allegati (lista non null e non vuota) li processiamo
        if (files != null && !files.isEmpty()) {
            // Cicla tutti i file ricevuti dal multipart
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;

                // Carica il file su Cloudinary e ottiene l'URL pubblico (https)
                String url = upload(file);

                // Determina se è immagine, video o file generico basandosi sul content-type
                MediaType type = detect(file);

                // Crea un'entità PostMedia collegata al post, salvando type e url
                PostMedia media = new PostMedia(post, type, url);

                // Aggiunge il media alla collection del post e imposta il back-reference
                post.addMedia(media);
            }
        }

        // Salva il Post nel DB.
        // CascadeType.ALL su Post.media fa salvare anche i PostMedia associati
        Post saved = postRepository.save(post);
        return toResponse(saved, currentUser);
    }


    // -------------- Update post + add/remove media -------------------
    @Transactional
    public PostResponseDTO updatePost(
            User currentUser,
            UUID postId,
            PostUpdateDTO body,
            List<UUID> mediaToRemove,
            List<MultipartFile> filesToAdd
    ) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        //permessi: author or ADMIN
        boolean isOwner = post.getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new UnauthorizedException("You are not allowed to edit this post");
        }

        //aggiorna campi solo se arrivano
        if (body != null) {
            if (body.title() != null) post.setTitle(body.title());
            if (body.description() != null) post.setDescription(body.description());
            if (body.externalUrl() != null) post.setExternalUrl(body.externalUrl());
        }

        // rimuove media (dal DB). orphanRemoval=true li cancella dalla tabella
        if (mediaToRemove != null && !mediaToRemove.isEmpty()) {
            post.getMedia().removeIf(m -> mediaToRemove.contains(m.getId()));
        }

        // Aggiunge nuovi file
        if (filesToAdd != null && !filesToAdd.isEmpty()) {
            for (MultipartFile file : filesToAdd) {
                if (file == null || file.isEmpty()) continue;

                String url = upload(file);
                MediaType type = detect(file);

                PostMedia pm = new PostMedia(post, type, url);
                post.addMedia(pm);
            }
        }
        Post saved = postRepository.save(post);
        return toResponse(saved, currentUser);
    }


    // ----------------------- delete post ------------------
    @Transactional
    public void deletePost(User currentUser, UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        //permessi: author or ADMIN
        boolean isOwner = post.getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new UnauthorizedException("You are not allowed to edit this post");
        }
        postRepository.delete(post);

    }

    // ------------------------  Find all posts --------------------
    public Page<PostResponseDTO> findAll(int page, int size, String sortBy, User currentUser) {
        if (size > 100)
            size = 100;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        Page<Post> posts = postRepository.findAll(pageable);
        return posts.map(p -> toResponse(p));
    }

    // ------------------------  Find by author --------------------
    public Page<PostResponseDTO> findByAuthor(
            UUID authorId,
            int page,
            int size,
            String sortBy,
            User currentUser
    ) {
        if (size > 100) size = 100;

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, sortBy)
        );

        Page<Post> posts = postRepository.findByAuthor_Id(authorId, pageable);

        return posts.map(p -> toResponse(p, currentUser));
    }


    /**
     * --------------------------------------- Post Likes ------------------------------------
     */
    public PostLikeStatusDTO getLikeStatus(User currentUser, UUID postId) {
        if (!postRepository.existsById(postId)) {
            throw new NotFoundException("Post not found");
        }
        long count = postLikeRepository.countByPost_Id(postId);

        boolean likedByMe = postLikeRepository
                .findByPost_IdAndUser_Id(postId, currentUser.getId())
                .isPresent();

        return new PostLikeStatusDTO(count, likedByMe);

    }


    @Transactional
    public PostLikeStatusDTO likePost(User currentUser, UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        // Se essiste gia, non creare doppioni
        if (postLikeRepository.findByPost_IdAndUser_Id(postId, currentUser.getId()).isEmpty()) {
            postLikeRepository.save(new PostLike(post, currentUser));
        }
        return getLikeStatus(currentUser, postId);
    }

    @Transactional
    public PostLikeStatusDTO unlikePost(User currentUser, UUID postId) {
        // solo se l'utente ha messo il like puo toglierlo
        PostLike like = postLikeRepository.findByPost_IdAndUser_Id(postId, currentUser.getId())
                .orElseThrow(() -> new BadRequestException("You have not liked this post"));

        postLikeRepository.delete(like);

        return getLikeStatus(currentUser, postId);
    }

    public PostLikeUsersDTO getLikeUsers(UUID postId) {
        if (!postRepository.existsById(postId)) {
            throw new NotFoundException("Post not found");
        }

        long count = postLikeRepository.countByPost_Id(postId);

        List<UserPublicDTO> users = postLikeRepository.findLikersByPostId(postId)
                .stream()
                .map(u -> new UserPublicDTO(
                        u.getId(),
                        u.getName(),
                        u.getSurname(),
                        u.getAvatar(),
                        u.getRole()))
                .toList();

        return new PostLikeUsersDTO(count, users);
    }

    //------------------------Share post ------------------------
    @Transactional
    public void sharePost(User currentUser, UUID postId, SharePostDTO body) {
        if (body == null || body.recipientIds().isEmpty()) {
            throw new BadRequestException("recipientId is required");
        }
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        String msg = body.message() != null ? body.message().trim() : null;
        if (msg != null && msg.isBlank()) msg = null;

        for (UUID rid : body.recipientIds()) {
            User recipient = userService.findById(rid);
            // evita share a se stesso
            if (recipient.getId().equals(currentUser.getId())) continue;

            PostShare share = new PostShare(post, currentUser, recipient, msg);
            postShareRepository.save(share);
        }
    }

    // -----------------------Inbox - post ricevuti ------------------

    private PostShareResponseDTO toShareDTO(PostShare s) {
        UserPublicDTO sender = new UserPublicDTO(
                s.getSender().getId(), s.getSender().getName(), s.getSender().getSurname(),
                s.getSender().getAvatar(), s.getSender().getRole()
        );
        UserPublicDTO recipient = new UserPublicDTO(
                s.getRecipient().getId(), s.getRecipient().getName(), s.getRecipient().getSurname(),
                s.getRecipient().getAvatar(), s.getRecipient().getRole()
        );
        return new PostShareResponseDTO(
                s.getId(),
                s.getPost().getId(),
                sender,
                recipient,
                s.getMessage(),
                s.getCreatedAt(),
                s.getReadAt() != null
        );

    }

    public Page<PostShareResponseDTO> inbox(User currentUser, int page, int size) {
        if (size > 100) size = 100;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return postShareRepository.findByRecipient_Id(currentUser.getId(), pageable).map(this::toShareDTO);
    }
}

