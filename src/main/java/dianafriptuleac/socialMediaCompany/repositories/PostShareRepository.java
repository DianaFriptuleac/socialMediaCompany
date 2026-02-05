package dianafriptuleac.socialMediaCompany.repositories;

import dianafriptuleac.socialMediaCompany.entities.PostShare;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostShareRepository extends JpaRepository<PostShare, UUID> {
    Page<PostShare> findByRecipient_Id(UUID recipientId, Pageable pageable);

    Page<PostShare> findBySender_Id(UUID senderId, Pageable pageable);
}
