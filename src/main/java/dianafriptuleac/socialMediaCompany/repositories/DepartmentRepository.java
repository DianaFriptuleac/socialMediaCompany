package dianafriptuleac.socialMediaCompany.repositories;

import dianafriptuleac.socialMediaCompany.entities.Department;
import dianafriptuleac.socialMediaCompany.enums.DepartmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    Optional<Department> findByDepartmentType(DepartmentType departmentType);
}
