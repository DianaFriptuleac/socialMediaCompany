package dianafriptuleac.socialMediaCompany.repositories;

import dianafriptuleac.socialMediaCompany.entities.UserDepartmentRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserDepartmentRoleRepository extends JpaRepository<UserDepartmentRole, UUID> {
    boolean existsByUserIdAndDepartmentIdAndRole(UUID userId, UUID departmentId, String role);

    void deleteByUserIdAndDepartmentIdAndRole(UUID userId, UUID departmentId, String role);

    void deleteAllByUserIdAndDepartmentId(UUID userId, UUID departmentId);


    List<UserDepartmentRole> findByUserId(UUID userId);

    List<UserDepartmentRole> findByUserIdAndDepartmentId(UUID userId, UUID departmentId);


    @Query("""
                select count(distinct udr.user.id)
                from UserDepartmentRole udr
                where udr.department.id = :departmentId
            """)
    long countUsersInDepartment(@Param("departmentId") UUID departmentId);

    long countByDepartmentIdAndRole(UUID departmentId, String role);
}
