package com.Passman.Manager.RolesManagement.Controllers;


import com.Passman.Manager.Auth.Models.User;
import com.Passman.Manager.Auth.Security.MyUserDetails;
import com.Passman.Manager.RolesManagement.DTO.*;
import com.Passman.Manager.RolesManagement.Models.Role;
import com.Passman.Manager.RolesManagement.Services.RolesManagementService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/management")
public class RolesManagementController {

    private final RolesManagementService rolesManagementService;

    public RolesManagementController(RolesManagementService rolesManagementService) {
        this.rolesManagementService = rolesManagementService;
    }

    @GetMapping("/entries/all")   // слеш в конце убран
    public List<EntryShowDTO> findAllEntriesShows(@AuthenticationPrincipal MyUserDetails currentUser) {
        List<EntryShowDTO> list = rolesManagementService.findAllAccessibleAsDtoShow(currentUser.getUser());
        list.forEach(System.out::println);
        return rolesManagementService.findAllAccessibleAsDtoShow(currentUser.getUser());
    }

    @GetMapping("/assignable-roles")
    public List<RoleDTO> getAssignableRoles(@AuthenticationPrincipal MyUserDetails currentUser) {
        return rolesManagementService.getAssignableRoles(currentUser);
    }

    @GetMapping("/department-workers")
    public List<UserDTO> getDepartmentWorkers(@AuthenticationPrincipal MyUserDetails currentUser) {
        return rolesManagementService.getDepartmentWorkers(currentUser);
    }

    @PostMapping("/assign-role")
    public void assignRoleToUser(@AuthenticationPrincipal MyUserDetails currentUser,
                                 @RequestBody AssignRoleDTO assignRoleDTO) {
        rolesManagementService.assignRoleToUser(
                currentUser,
                assignRoleDTO.getTargetUserId(),
                assignRoleDTO.getRoleId()
        );
    }

    @PostMapping("/remove-role")
    public void removeRoleFromUser(@AuthenticationPrincipal MyUserDetails currentUser,
                                   @RequestBody AssignRoleDTO assignRoleDTO) {
        rolesManagementService.removeRoleFromUser(
                currentUser,
                assignRoleDTO.getTargetUserId(),
                assignRoleDTO.getRoleId()
        );
    }

    @PostMapping("/assign-role-access")
    public void grantAccessToRole(@AuthenticationPrincipal MyUserDetails currentUser,
                                  @RequestBody AccessRightsDTO accessRightsDTO) {
        rolesManagementService.grantAccessToRole(currentUser, accessRightsDTO);
    }

    @PostMapping("/assign-user-access")
    public void grantAccessToUser(@AuthenticationPrincipal MyUserDetails currentUser,
                                  @RequestBody UserAccessRightsDTO dto) {
        rolesManagementService.grantAccessToUser(currentUser, dto);
    }

    @DeleteMapping("/revoke-user-access")
    public void revokeAccessFromUser(@AuthenticationPrincipal MyUserDetails currentUser,
                                     @RequestParam Long targetUserId,
                                     @RequestParam Long entryId) {
        rolesManagementService.revokeAccessFromUser(currentUser, targetUserId, entryId);
    }
}
