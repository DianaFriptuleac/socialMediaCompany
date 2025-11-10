package dianafriptuleac.socialMediaCompany.controllers;

import dianafriptuleac.socialMediaCompany.payloads.AssignRoleDTO;
import dianafriptuleac.socialMediaCompany.services.DepartmentMembershipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/departments")
public class DepartmentMembershipController {

    private final DepartmentMembershipService departmentMembershipService;

    public DepartmentMembershipController(DepartmentMembershipService departmentMembershipService) {
        this.departmentMembershipService = departmentMembershipService;
    }

    @PostMapping("/assign")
    public ResponseEntity<String> assignRoles(@RequestBody AssignRoleDTO dto) {
        departmentMembershipService.assignRoles(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Roles assigned correctly");
    }
}
