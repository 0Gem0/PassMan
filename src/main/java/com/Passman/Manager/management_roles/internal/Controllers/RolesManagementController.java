package com.Passman.Manager.management_roles.internal.Controllers;


import com.Passman.Manager.shared.Security.UserPrincipal;
import com.Passman.Manager.management_roles.DTO.*;
import com.Passman.Manager.management_roles.internal.Services.RolesManagementService;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<UserPublicKeyDTO> getPublicKey(@AuthenticationPrincipal UserPrincipal currentUser,
                                                         @PathVariable long id) {
        return ResponseEntity.ok(rolesManagementService.getUserPublicKey(currentUser.getId(), id));
    }

    @PostMapping("/share-entry")
    public ResponseEntity<String> shareEntry(@AuthenticationPrincipal UserPrincipal currentUser,
                                             @RequestBody ShareEntryDTO shareEntryDTO) {
        rolesManagementService.shareEntry(currentUser.getId(), shareEntryDTO);
        return ResponseEntity.ok("Entry shared successfully");
    }

    @PostMapping("/roles")
    public ResponseEntity<String> createRole(@AuthenticationPrincipal UserPrincipal currentUser,
                                             @RequestBody CreateRoleDTO dto) {
        rolesManagementService.createRole(currentUser.getId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Role created");
    }

    @GetMapping("/entries/all")
    public ResponseEntity<List<EntryRolesDTO>> findAllEntriesShows(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(rolesManagementService.findAllAccessibleAsDtoShow(currentUser.getId()));
    }

    @GetMapping("/roles/{roleId}/users")
    public ResponseEntity<List<UserPublicKeyDTO>> getRoleWorkers(@AuthenticationPrincipal UserPrincipal currentUser,
                                                                 @PathVariable Long roleId) {
        return ResponseEntity.ok(rolesManagementService.findUsersByRole(roleId, currentUser.getId()));
    }

    @GetMapping("/assignable-roles")
    public ResponseEntity<List<RoleDTO>> getAssignableRoles(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(rolesManagementService.getAssignableRoles(currentUser.getId()));
    }

    @GetMapping("/department-workers")
    public ResponseEntity<List<UserPublicKeyDTO>> getDepartmentWorkers(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(rolesManagementService.getDepartmentWorkers(currentUser.getId()));
    }

    @GetMapping("/departments/assignable")
    public ResponseEntity<List<DepartmentDTO>> getAssignableDepartments(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(rolesManagementService.getAssignableDepartments(currentUser.getId()));
    }

    @PatchMapping("/users/department")
    public ResponseEntity<String> assignDepartment(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody AssignDepartmentDTO assignDepartmentDTO) {
        rolesManagementService.assignDepartment(currentUser.getId(), assignDepartmentDTO);
        return ResponseEntity.ok("Department assigned");
    }

    @PostMapping("/assign-role")
    public ResponseEntity<String> assignRoleToUser(@AuthenticationPrincipal UserPrincipal currentUser,
                                                   @RequestBody AssignRoleDTO assignRoleDTO) {
        rolesManagementService.assignRoleToUser(
                currentUser.getId(),
                assignRoleDTO.getTargetUserId(),
                assignRoleDTO.getRoleId()
        );
        return ResponseEntity.ok("Role assigned to user");
    }

    @PostMapping("/remove-role")
    public ResponseEntity<String> removeRoleFromUser(@AuthenticationPrincipal UserPrincipal currentUser,
                                                     @RequestBody AssignRoleDTO assignRoleDTO) {
        rolesManagementService.removeRoleFromUser(
                currentUser.getId(),
                assignRoleDTO.getTargetUserId(),
                assignRoleDTO.getRoleId()
        );
        return ResponseEntity.ok("Role removed from user");
    }

    @PostMapping("/assign-role-access")
    public ResponseEntity<String> grantAccessToRole(@AuthenticationPrincipal UserPrincipal currentUser,
                                                    @RequestBody AccessRightsDTO accessRightsDTO) {
        rolesManagementService.grantAccessToRole(currentUser.getId(), accessRightsDTO);
        return ResponseEntity.ok("Access granted to role");
    }

    @PostMapping("/assign-user-access")
    public ResponseEntity<String> grantAccessToUser(@AuthenticationPrincipal UserPrincipal currentUser,
                                                    @RequestBody UserAccessRightsDTO dto) {
        rolesManagementService.grantAccessToUser(currentUser.getId(), dto);
        return ResponseEntity.ok("Access granted to user");
    }

    @DeleteMapping("/revoke-user-access")
    public ResponseEntity<String> revokeAccessFromUser(@AuthenticationPrincipal UserPrincipal currentUser,
                                                       @RequestParam Long targetUserId,
                                                       @RequestParam Long entryId) {
        rolesManagementService.revokeAccessFromUser(currentUser.getId(), targetUserId, entryId);
        return ResponseEntity.ok("Access revoked from user");
    }

    @DeleteMapping("/revoke-role-access")
    public ResponseEntity<String> revokeAccessFromRole(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam Long roleId,
            @RequestParam Long entryId) {
        rolesManagementService.revokeAccessFromRole(currentUser.getId(), roleId, entryId);
        return ResponseEntity.ok("Access revoked from role");
    }
}
