package dianafriptuleac.socialMediaCompany.repositories;

import dianafriptuleac.socialMediaCompany.entities.PostShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostShareRepository extends JpaRepository<PostShare, UUID> {
    long countByPost_Id(UUID postId);
}
