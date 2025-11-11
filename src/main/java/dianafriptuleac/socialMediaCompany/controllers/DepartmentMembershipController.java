package dianafriptuleac.socialMediaCompany.controllers;

import dianafriptuleac.socialMediaCompany.entities.Department;
import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.enums.DepartmentRole;
import dianafriptuleac.socialMediaCompany.exceptions.NotFoundException;
import dianafriptuleac.socialMediaCompany.payloads.AssignRoleDTO;
import dianafriptuleac.socialMediaCompany.payloads.DepartmentCreateDTO;
import dianafriptuleac.socialMediaCompany.payloads.DepartmentWithUserDTO;
import dianafriptuleac.socialMediaCompany.payloads.UserWithRolesInDepartmentDTO;
import dianafriptuleac.socialMediaCompany.services.DepartmentMembershipService;
import org.springframework.http.HttpStatus;
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
                    List<DepartmentRole> roles = entry.getValue().stream()
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
                department.getDepartmentType().name(),
                department.getDescription(),
                users.size(),
                users
        );
    }
}
