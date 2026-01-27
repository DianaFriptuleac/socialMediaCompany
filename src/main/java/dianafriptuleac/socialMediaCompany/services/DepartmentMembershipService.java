package dianafriptuleac.socialMediaCompany.services;

import dianafriptuleac.socialMediaCompany.entities.Department;
import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.entities.UserDepartmentRole;
import dianafriptuleac.socialMediaCompany.exceptions.BadRequestException;
import dianafriptuleac.socialMediaCompany.exceptions.NotFoundException;
import dianafriptuleac.socialMediaCompany.payloads.AssignRoleDTO;
import dianafriptuleac.socialMediaCompany.payloads.DepartmentCreateDTO;
import dianafriptuleac.socialMediaCompany.payloads.UpdateDepartmentDTO;
import dianafriptuleac.socialMediaCompany.payloads.UserDepartmentRolesViewDTO;
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
        if (departmentCreateDTO == null || departmentCreateDTO.name().isBlank()) {
            throw new IllegalArgumentException("departmentType is required");
        }
        String normalizedName = departmentCreateDTO.name().trim();
        //evita duplicati
        departmentRepository.findByNameIgnoreCase(normalizedName).ifPresent(d -> {
            throw new BadRequestException("Department already exists: " + normalizedName);
        });
        Department department = new Department(normalizedName, departmentCreateDTO.description());
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
        Set<String> currentRoles = current.stream().map(UserDepartmentRole::getRole).collect(Collectors.toSet());

        // aggiunge solo quelli mancanti
        List<UserDepartmentRole> toSave = new ArrayList<>();
        for (String role : dto.roles()) {
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

    /* -------------------- NR OF USERS X DEPARTMENT-------------------- */
    public long getUserCountInDepartment(UUID departmentId) {
        return userDepartmentRoleRepository.countUsersInDepartment(departmentId);
    }

    /* -------------------- NR OF ROLE USERS X DEPARTMENT-------------------- */
    public long countUsersByDepartmentAndRole(UUID departmentId, String role) {
        return userDepartmentRoleRepository.countByDepartmentIdAndRole(departmentId, role);
    }

    /* -------------------- DEPARTMENTS OF A USER -------------------- */
    @Transactional
    public List<UserDepartmentRolesViewDTO> getDepartmentsForUser(UUID userId) {
        // prendo tutte le righe user_department_roles per questo utente
        List<UserDepartmentRole> memberships = userDepartmentRoleRepository.findByUserId(userId);

        // raggruppo per departmentId e costruisco il DTO di risposta
        return memberships.stream()
                .collect(Collectors.groupingBy(m -> m.getDepartment().getId()))
                .entrySet()
                .stream()
                .map(entry -> {
                    UUID departmentId = entry.getKey();
                    // prendo il department da una delle membership del gruppo
                    Department dept = entry.getValue().getFirst().getDepartment();

                    // lista ruoli (senza duplicati) in questo department
                    List<String> roles = entry.getValue().stream()
                            .map(UserDepartmentRole::getRole)
                            .distinct()
                            .toList();

                    return new UserDepartmentRolesViewDTO(
                            departmentId,
                            dept.getName(),
                            roles
                    );
                })
                .toList();
    }

    //Delete user from department
    @Transactional
    public void removeUserFromDepartment(UUID userId, UUID departmentId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found!");
        }
        if (!departmentRepository.existsById(departmentId)) {
            throw new NotFoundException("Department not found!");
        }
        userDepartmentRoleRepository.deleteAllByUserIdAndDepartmentId(userId, departmentId);
    }

    // delete department role from user
    @Transactional
    public void removeDepartmentRoleFromUser(UUID userId, UUID departmentId, String role) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found!");
        }
        if (!departmentRepository.existsById(departmentId)) {
            throw new NotFoundException("Department not found!");
        }
        if (role == null | role.isBlank()) {
            throw new BadRequestException("Role is required");
        }
        String normalizedRole = role.trim().toUpperCase();

        userDepartmentRoleRepository.deleteByUserIdAndDepartmentIdAndRole(
                userId,
                departmentId,
                normalizedRole
        );
    }

    // delete department
    @Transactional
    public void deleteDepartment(UUID departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new NotFoundException("Department not found!");
        }
        // cancella le righe user_department_roles collegate
        userDepartmentRoleRepository.deleteAllByDepartmentId(departmentId);

        //candella il department
        departmentRepository.deleteById(departmentId);
    }

    // Update department name and description
    @Transactional
    public Department updateDepartment(UUID departmentId, UpdateDepartmentDTO updateDepartmentDTO) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new NotFoundException("Department not found!"));
        if (updateDepartmentDTO == null) throw new BadRequestException("Body is required!");
        // update name
        if (updateDepartmentDTO.name() != null && !updateDepartmentDTO.name().isBlank()) {
            String normalizedName = updateDepartmentDTO.name().trim();


            departmentRepository.findByNameIgnoreCase(normalizedName).ifPresent(existing -> {
                if (!existing.getId().equals(departmentId)) {
                    throw new BadRequestException("Department name already exists: " + normalizedName);
                }
            });
            department.setName(normalizedName);
        }

        // update description
        if (updateDepartmentDTO.description() != null) {
            department.setDescription(updateDepartmentDTO.description().trim());
        }
        return departmentRepository.save(department);
    }
}