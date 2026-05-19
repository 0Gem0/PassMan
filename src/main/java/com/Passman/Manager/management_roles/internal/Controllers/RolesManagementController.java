package com.Passman.Manager.management_roles.internal.Controllers;


import com.Passman.Manager.shared.Security.MyUserDetails;
import com.Passman.Manager.management_roles.DTO.*;
import com.Passman.Manager.management_roles.internal.Services.RolesManagementService;
import com.Passman.Manager.vault.DTO.EntryDTO;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/users/{id}/public-key")
    public UserPublicKeyDTO getPublicKey(@AuthenticationPrincipal MyUserDetails currentUser, @PathVariable long id){
        return rolesManagementService.getUserPublicKey(currentUser.getId(), id);
    }

    @PostMapping("/share-entry")
    public ResponseEntity<?> shareEntry(@AuthenticationPrincipal MyUserDetails currentUser,@RequestBody ShareEntryDTO shareEntryDTO){
        rolesManagementService.shareEntry(currentUser.getId(),shareEntryDTO);
        return ResponseEntity.ok("Ok");
    }

    @PostMapping("/roles")
    public void createRole(@AuthenticationPrincipal MyUserDetails currentUser,
                           @RequestBody CreateRoleDTO dto) {
        rolesManagementService.createRole(currentUser, dto);
    }

    @GetMapping("/entries/all")
    public List<EntryDTO> findAllEntriesShows(@AuthenticationPrincipal MyUserDetails currentUser) {
        List<EntryDTO> list = rolesManagementService.findAllAccessibleAsDtoShow(currentUser.getUser());
        list.forEach(System.out::println);
        return rolesManagementService.findAllAccessibleAsDtoShow(currentUser.getUser());
    }

    @GetMapping("/roles/{roleId}/users")
    public List<UserPublicKeyDTO> getRoleWorkers(@AuthenticationPrincipal MyUserDetails currentUser, @PathVariable Long roleId){
        return rolesManagementService.findUsersByRole(roleId, currentUser.getId());
    }

    @GetMapping("/assignable-roles")
    public List<RoleDTO> getAssignableRoles(@AuthenticationPrincipal MyUserDetails currentUser) {
        return rolesManagementService.getAssignableRoles(currentUser);
    }

    @GetMapping("/department-workers")
    public List<UserPublicKeyDTO> getDepartmentWorkers(@AuthenticationPrincipal MyUserDetails currentUser) {
        return rolesManagementService.getDepartmentWorkers(currentUser);
    }

    @GetMapping("/departments/assignable")
    public List<DepartmentDTO> getAssignableDepartments(
            @AuthenticationPrincipal MyUserDetails currentUser
    ) {
        return rolesManagementService.getAssignableDepartments(currentUser);
    }

    @PatchMapping("/users/department")
    public void assignDepartment(
            @AuthenticationPrincipal MyUserDetails currentUser,
            @RequestBody AssignDepartmentDTO dto
    ) {
        rolesManagementService.assignDepartment(currentUser, dto);
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

    @DeleteMapping("/revoke-role-access")
    public void revokeAccessFromRole(
            @AuthenticationPrincipal MyUserDetails currentUser,
            @RequestParam Long roleId,
            @RequestParam Long entryId
    ) {
        rolesManagementService.revokeAccessFromRole(currentUser, roleId, entryId);
    }
}
