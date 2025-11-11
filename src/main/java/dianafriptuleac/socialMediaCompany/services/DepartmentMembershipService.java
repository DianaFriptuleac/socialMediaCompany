package dianafriptuleac.socialMediaCompany.services;

import dianafriptuleac.socialMediaCompany.entities.Department;
import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.entities.UserDepartmentRole;
import dianafriptuleac.socialMediaCompany.enums.DepartmentRole;
import dianafriptuleac.socialMediaCompany.exceptions.NotFoundException;
import dianafriptuleac.socialMediaCompany.payloads.AssignRoleDTO;
import dianafriptuleac.socialMediaCompany.payloads.DepartmentCreateDTO;
import dianafriptuleac.socialMediaCompany.repositories.DepartmentRepository;
import dianafriptuleac.socialMediaCompany.repositories.UserDepartmentRoleRepository;
import dianafriptuleac.socialMediaCompany.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DepartmentMembershipService {
    private UserRepository userRepository;
    private DepartmentRepository departmentRepository;
    private UserDepartmentRoleRepository userDepartmentRoleRepository;

    public DepartmentMembershipService(UserRepository userRepository,
                                       DepartmentRepository departmentRepository,
                                       UserDepartmentRoleRepository userDepartmentRoleRepository) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.userDepartmentRoleRepository = userDepartmentRoleRepository;
    }

    /* -------------------- DEPARTMENTS -------------------- */
    @Transactional
    public Department createDepartment(DepartmentCreateDTO departmentCreateDTO) {
        if (departmentCreateDTO == null || departmentCreateDTO.departmentType() == null) {
            throw new IllegalArgumentException("departmentType is required");
        }
        //evita duplicati
        departmentRepository.findByDepartmentType(departmentCreateDTO.departmentType()).ifPresent(d -> {
            throw new IllegalArgumentException("Department already exists: " + departmentCreateDTO.departmentType());
        });
        Department department = new Department(departmentCreateDTO.departmentType(), departmentCreateDTO.description());
        return departmentRepository.save(department);
    }

    @Transactional
    public List<Department> departmentList() {
        return departmentRepository.findAll();
    }

    @Transactional
    public Optional<Department> findByIdFetchUsers(UUID id) {
        return departmentRepository.findByIdFetchUsers(id);
    }

    /* -------------------- ROLES ASSIGNMENT -------------------- */
    @Transactional
    public void assignRoles(AssignRoleDTO dto) {
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Department department = departmentRepository.findById(dto.departmentId())
                .orElseThrow(() -> new NotFoundException("Department not found"));

        if (dto.roles() == null || dto.roles().isEmpty()) return;

        // ruoli già presenti per (user, department)
        List<UserDepartmentRole> current = userDepartmentRoleRepository.findByUserIdAndDepartmentId(
                user.getId(), department.getId()
        );
        Set<DepartmentRole> currentRoles = current.stream().map(UserDepartmentRole::getRole).collect(Collectors.toSet());

        // aggiunge solo quelli mancanti
        List<UserDepartmentRole> toSave = new ArrayList<>();
        for (DepartmentRole role : dto.roles()) {
            if (!currentRoles.contains(role)) {
                UserDepartmentRole membership = UserDepartmentRole.builder()
                        .user(user)
                        .department(department)
                        .role(role)
                        .build();
                toSave.add(membership);
            }
        }
        if (!toSave.isEmpty()) {
            userDepartmentRoleRepository.saveAll(toSave);
        }
    }


}