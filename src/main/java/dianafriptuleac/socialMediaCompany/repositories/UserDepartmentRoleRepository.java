package dianafriptuleac.socialMediaCompany.repositories;

import dianafriptuleac.socialMediaCompany.entities.UserDepartmentRole;
import dianafriptuleac.socialMediaCompany.enums.DepartmentRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserDepartmentRoleRepository extends JpaRepository<UserDepartmentRole, UUID> {
    boolean existsByUserIdAndDepartmentIdAndRole(UUID userId, UUID departmentId, DepartmentRole role);

    void deleteByUserIdAndDepartmentIdAndRole(UUID userId, UUID departmentId, DepartmentRole role);

    List<UserDepartmentRole> findByUserId(UUID userId);
}
