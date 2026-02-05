package dianafriptuleac.socialMediaCompany.repositories;

import dianafriptuleac.socialMediaCompany.entities.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByPost_IdAndParentIsNullOrderByCreatedAtAsc(UUID postId);

    long countByPost_Id(UUID postId);

    // Prende i commenti root (non-reply) di un post
    @Query("""
                select c
                from Comment c
                where c.post.id = :postId and c.parent is null
                order by c.createdAt asc
            """)
    List<Comment> findRootByPostId(@Param("postId") UUID postId);
}
