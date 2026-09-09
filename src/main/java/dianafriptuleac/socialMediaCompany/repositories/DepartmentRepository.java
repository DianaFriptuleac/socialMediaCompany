package dianafriptuleac.socialMediaCompany.repositories;

import dianafriptuleac.socialMediaCompany.entities.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    Optional<Department> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    @Query("""
                select d from Department d
                left join fetch d.memberships m
                left join fetch m.user u
                where d.id = :id
            """)
    Optional<Department> findByIdFetchUsers(@Param("id") UUID id);

    // Search departments
    @Query("""
            SELECT d
            FROM Department d
            WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(d.description) LIKE LOWER(CONCAT('%', :query, '%'))
            ORDER BY d.name ASC
            """)
    Page<Department> serachDepartments(@Param("query") String query, Pageable pageable);
}
