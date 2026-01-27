package dianafriptuleac.socialMediaCompany.controllers;

import dianafriptuleac.socialMediaCompany.entities.Department;
import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.exceptions.NotFoundException;
import dianafriptuleac.socialMediaCompany.payloads.*;
import dianafriptuleac.socialMediaCompany.services.DepartmentMembershipService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/departments")
public class DepartmentMembershipController {

    private final DepartmentMembershipService departmentMembershipService;

    public DepartmentMembershipController(DepartmentMembershipService departmentMembershipService) {
        this.departmentMembershipService = departmentMembershipService;
    }

    // Crea un nuovo reparto
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Department createDepartment(@RequestBody DepartmentCreateDTO dto) {
        return departmentMembershipService.createDepartment(dto);

    }

    // Lista tutti i reparti
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Department> listDepartments() {
        return departmentMembershipService.departmentList();
    }

    // Assegna ruoli a un utente per un reparto
    @PostMapping("/assign")
    @ResponseStatus(HttpStatus.CREATED)
    public void assignRoles(@RequestBody AssignRoleDTO dto) {
        departmentMembershipService.assignRoles(dto);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DepartmentWithUserDTO getDepartmentWithUsers(@PathVariable UUID id) {
        Department department = departmentMembershipService.findByIdFetchUsers(id)
                .orElseThrow(() -> new NotFoundException("Department not found"));

        List<UserWithRolesInDepartmentDTO> users = department.getMemberships().stream()
                .collect(Collectors.groupingBy(udr -> udr.getUser().getId()))
                .entrySet().stream()
                .map(entry -> {
                    User user = entry.getValue().get(0).getUser();
                    List<String> roles = entry.getValue().stream()
                            .map(udr -> udr.getRole())
                            .distinct()
                            .toList();
                    return new UserWithRolesInDepartmentDTO(
                            user.getId(),
                            user.getName(),
                            user.getSurname(),
                            user.getEmail(),
                            user.getAvatar(),
                            roles
                    );
                })
                .toList();

        return new DepartmentWithUserDTO(
                department.getId(),
                department.getName(),
                department.getDescription(),
                users.size(),
                users
        );
    }

    // Calcola nr. utendi nel department
    @GetMapping("/{id}/count")
    @ResponseStatus(HttpStatus.OK)
    public long countUsersInDepartment(@PathVariable("id") UUID id) {
        return departmentMembershipService.getUserCountInDepartment(id);
    }

    //Calcola nr. ruoli x department
    @GetMapping("/{id}/count/{role}")
    public long countUsersInDepartmentByRole(
            @PathVariable UUID id,
            @PathVariable String role
    ) {
        return departmentMembershipService.countUsersByDepartmentAndRole(id, role);
    }

    //Delete user from department
    @DeleteMapping("/{departmentId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUserFromDepartment(
            @PathVariable UUID departmentId,
            @PathVariable UUID userId
    ) {
        departmentMembershipService.removeUserFromDepartment(userId, departmentId);
    }

    // Delete department role from user
    @DeleteMapping("/{departmentId}/members/{userId}/roles/{role}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeDepartmentRoleFromUser(
            @PathVariable UUID departmentId,
            @PathVariable UUID userId,
            @PathVariable String role
    ) {
        departmentMembershipService.removeDepartmentRoleFromUser(userId, departmentId, role);
    }

    // delete department
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDepartment(
            @PathVariable UUID id
    ) {
        departmentMembershipService.deleteDepartment(id);
    }

    // update deparment name and description
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public Department updateDepartment(
            @PathVariable UUID id,
            @RequestBody UpdateDepartmentDTO updateDepartmentDTO
    ) {
        return departmentMembershipService.updateDepartment(id, updateDepartmentDTO);
    }
}
