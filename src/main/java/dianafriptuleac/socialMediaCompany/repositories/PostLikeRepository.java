package dianafriptuleac.socialMediaCompany.repositories;

import dianafriptuleac.socialMediaCompany.entities.PostLike;
import dianafriptuleac.socialMediaCompany.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {
    Optional<PostLike> findByPost_IdAndUser_Id(UUID postId, UUID userId);

    long countByPost_Id(UUID postId);

    @Query("""
                select pl.user
                from PostLike pl
                where pl.post.id = :postId
                order by pl.createdAt desc
            """)
    List<User> findLikersByPostId(@Param("postId") UUID postId);
}
