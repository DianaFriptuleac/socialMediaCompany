package dianafriptuleac.socialMediaCompany.controllers;

import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.exceptions.BadRequestException;
import dianafriptuleac.socialMediaCompany.payloads.UserDTO;
import dianafriptuleac.socialMediaCompany.payloads.UserDepartmentRolesViewDTO;
import dianafriptuleac.socialMediaCompany.services.DepartmentMembershipService;
import dianafriptuleac.socialMediaCompany.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private DepartmentMembershipService departmentMembershipService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<User> findAll(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "8") int size,
                              @RequestParam(defaultValue = "id") String sortBy) {
        return this.userService.findAll(page, size, sortBy);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public User findById(@PathVariable UUID userId) {
        return this.userService.findById(userId);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserById(@PathVariable UUID id) {
        userService.findByIdAndDelete(id);
    }

    @GetMapping("/me")
    public User getMyProfile(@AuthenticationPrincipal User currentAuthenticateUser) {
        User myDetails = userService.findById(currentAuthenticateUser.getId());
        return currentAuthenticateUser;
    }

    @PutMapping("/me")
    public User updateMyProfile(@AuthenticationPrincipal User currentAuthenticateUser,
                                @RequestBody @Validated UserDTO body) {
        return this.userService.findByIdAndUpdate(currentAuthenticateUser.getId(), body);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(@AuthenticationPrincipal User currentAuthenticatedUser) {
        userService.findByIdAndDelete(currentAuthenticatedUser.getId());
    }

    @PatchMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> uploadAvatar(
            @AuthenticationPrincipal User currentAuthenticatedUser,
            @RequestParam("avatar") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("The file was not received or is empty.");
        }
        System.out.println("File received: " + file.getOriginalFilename());
        return userService.uploadAvatar(currentAuthenticatedUser.getId(), file);
    }

    // Departments + Ruoli User
    @GetMapping("/me/departments")
    public List<UserDepartmentRolesViewDTO> getMyDepartments(
            @AuthenticationPrincipal User currentAuthenticatedUser
    ) {
        // uso l'ID dell'utente loggato per recuperare i suoi reparti + ruoli
        return departmentMembershipService.getDepartmentsForUser(currentAuthenticatedUser.getId());
    }


}
