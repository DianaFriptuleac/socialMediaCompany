package dianafriptuleac.socialMediaCompany.services;

import dianafriptuleac.socialMediaCompany.entities.Comment;
import dianafriptuleac.socialMediaCompany.entities.Post;
import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.enums.Role;
import dianafriptuleac.socialMediaCompany.exceptions.BadRequestException;
import dianafriptuleac.socialMediaCompany.exceptions.NotFoundException;
import dianafriptuleac.socialMediaCompany.exceptions.UnauthorizedException;
import dianafriptuleac.socialMediaCompany.payloads.Comment.CommentCreateDTO;
import dianafriptuleac.socialMediaCompany.payloads.Comment.CommentResponseDTO;
import dianafriptuleac.socialMediaCompany.payloads.Comment.UserPublicDTO;
import dianafriptuleac.socialMediaCompany.repositories.CommentRepository;
import dianafriptuleac.socialMediaCompany.repositories.PostRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    // -------- mapper (ricorsivo)
    private CommentResponseDTO toResponse(Comment c, boolean includeReplies) {
        User a = c.getAuthor();
        UserPublicDTO author = new UserPublicDTO(
                a.getId(),
                a.getName(),
                a.getSurname(),
                a.getAvatar(),
                a.getRole()
        );

        List<CommentResponseDTO> replies = List.of();
        if (includeReplies && c.getReplies() != null && !c.getReplies().isEmpty()) {
            // ordina per data, così le reply sono in ordine
            replies = c.getReplies().stream()
                    .sorted((x, y) -> x.getCreatedAt().compareTo(y.getCreatedAt()))
                    .map(r -> toResponse(r, true))
                    .toList();
        }

        return new CommentResponseDTO(
                c.getId(),
                c.getPost().getId(),
                author,
                c.getText(),
                c.getParent() != null ? c.getParent().getId() : null,
                replies,
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }

    // ----------Crea commento -------
    @Transactional
    public CommentResponseDTO create(User currentUser, UUID postId, CommentCreateDTO body) {
        if (body == null) throw new BadRequestException("Comment body is required");

        String text = body.text() != null ? body.text().trim() : "";
        if (text.isBlank()) throw new BadRequestException("Text is required");

        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Post not found"));

        Comment parent = null;
        if (body.parentCommentId() != null) {
            parent = commentRepository.findById(body.parentCommentId())
                    .orElseThrow(() -> new NotFoundException("Parent comment not found"));

            //IMPORTANTE: parent deve appartenere allo stesso post
            if (!parent.getPost().getId().equals(postId)) {
                throw new BadRequestException("Parent comment belongs to a different post");
            }
        }
        Comment comment = new Comment(post, currentUser, text, parent);
        Comment saved = commentRepository.save(comment);
        return toResponse(saved, true);
    }

    //Lista comments per post
    public List<CommentResponseDTO> getByPost(UUID postId) {
        if (!postRepository.existsById(postId)) {
            throw new NotFoundException("Post not found");
        }
        return commentRepository.findRootByPostId(postId).stream()
                .map(c -> toResponse(c, true)).toList();
    }

    // Delete comment (solo se autore commento, admin or autore post)
    @Transactional
    public void delete(User currentUser, UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        UUID commentAuthorId = comment.getAuthor().getId();
        UUID postAuthorId = comment.getPost().getAuthor().getId();

        boolean isCommentOwner = commentAuthorId.equals(currentUser.getId());
        boolean isPostOwner = postAuthorId.equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isCommentOwner && !isPostOwner && !isAdmin) {
            throw new UnauthorizedException("You are not allowed to delete this comment");
        }
        commentRepository.delete(comment);
    }

}
