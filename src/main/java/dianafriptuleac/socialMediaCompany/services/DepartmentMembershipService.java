package dianafriptuleac.socialMediaCompany.services;

import dianafriptuleac.socialMediaCompany.entities.Department;
import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.entities.UserDepartmentRole;
import dianafriptuleac.socialMediaCompany.enums.DepartmentRole;
import dianafriptuleac.socialMediaCompany.exceptions.NotFoundException;
import dianafriptuleac.socialMediaCompany.payloads.AssignRoleDTO;
import dianafriptuleac.socialMediaCompany.repositories.DepartmentRepository;
import dianafriptuleac.socialMediaCompany.repositories.UserDepartmentRoleRepository;
import dianafriptuleac.socialMediaCompany.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class DepartmentMembershipService {
    private UserRepository userRepository;
    private DepartmentRepository departmentRepository;
    private UserDepartmentRoleRepository membershipRepository;

    public DepartmentMembershipService(UserRepository userRepository,
                                       DepartmentRepository departmentRepository,
                                       UserDepartmentRoleRepository membershipRepository) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.membershipRepository = membershipRepository;
    }

    @Transactional
    public void assignRoles(AssignRoleDTO dto) {
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Department department = departmentRepository.findById(dto.departmentId())
                .orElseThrow(() -> new NotFoundException("Department not found"));

        for (DepartmentRole role : dto.roles()) {
            boolean exists = membershipRepository
                    .existsByUserIdAndDepartmentIdAndRole(user.getId(), department.getId(), role);
            if (!exists) {
                UserDepartmentRole membership = UserDepartmentRole.builder()
                        .user(user)
                        .department(department)
                        .role(role)
                        .build();
                membershipRepository.save(membership);
            }
        }
    }

}