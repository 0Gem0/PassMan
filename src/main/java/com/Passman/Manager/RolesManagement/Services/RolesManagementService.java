package com.Passman.Manager.RolesManagement.Services;

import com.Passman.Manager.Auth.Models.User;
import com.Passman.Manager.Auth.Repos.UserRepository;
import com.Passman.Manager.Auth.Security.MyUserDetails;
import com.Passman.Manager.RolesManagement.DTO.*;
import com.Passman.Manager.RolesManagement.Models.AccessRights;
import com.Passman.Manager.RolesManagement.Models.Role;
import com.Passman.Manager.RolesManagement.Models.UserAccessRights;
import com.Passman.Manager.RolesManagement.Repos.AccessRightsRepository;
import com.Passman.Manager.RolesManagement.Repos.RoleRepository;
import com.Passman.Manager.RolesManagement.Repos.UserAccessRightsRepository;
import com.Passman.Manager.Vault.Models.Entry;
import com.Passman.Manager.Vault.Repos.EntryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RolesManagementService {

    private final UserAccessRightsRepository userAccessRightsRepository;
    private final AccessRightsRepository accessRightsRepository;
    private final UserRepository userRepository;
    private final ModelMapper mapper;
    private final EntryRepository entryRepository;
    private final RoleRepository roleRepository;

    public RolesManagementService(UserAccessRightsRepository userAccessRightsRepository,
                                  AccessRightsRepository accessRightsRepository,
                                  UserRepository userRepository, ModelMapper mapper,
                                  EntryRepository entryRepository,
                                  RoleRepository roleRepository) {
        this.userAccessRightsRepository = userAccessRightsRepository;
        this.accessRightsRepository = accessRightsRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.entryRepository = entryRepository;
        this.roleRepository = roleRepository;
    }

    public boolean hasAccess(Long entryId, User user) {
        var userAccessRightsOptional = userAccessRightsRepository.findByUserAndEntryId(user, entryId);

        if (userAccessRightsOptional.isPresent()) {
            UserAccessRights userAccessRights = userAccessRightsOptional.get();
            return userAccessRights.isCanView() || userAccessRights.isCanEdit();
        }

        for (Role role : user.getRoles()) {
            var accessRightsOptional = accessRightsRepository.findByRoleAndEntryId(role, entryId);
            if (accessRightsOptional.isPresent()) {
                AccessRights accessRights = accessRightsOptional.get();
                if (accessRights.isCanView() || accessRights.isCanEdit()) {
                    return true;
                }
            }
        }

        return false;
    }

    public List<EntryShowDTO> findAllAccessibleAsDtoShow(User user) {
        if (isAdmin(user)){
            return entryRepository.findAll().stream().map(entry -> mapper.map(entry, EntryShowDTO.class)).collect(Collectors.toList());
        }
        return entryRepository.findAccessibleEntries(user.getId())
                .stream()
                .map(entry -> mapper.map(entry, EntryShowDTO.class))
                .collect(Collectors.toList());
    }
    public boolean hasEditAccess(Long entryId, User user) {
        var userAccessRightsOptional = userAccessRightsRepository.findByUserAndEntryId(user, entryId);

        if (userAccessRightsOptional.isPresent()) {
            return userAccessRightsOptional.get().isCanEdit();
        }

        for (Role role : user.getRoles()) {
            var accessRightsOptional = accessRightsRepository.findByRoleAndEntryId(role, entryId);
            if (accessRightsOptional.isPresent() && accessRightsOptional.get().isCanEdit()) {
                return true;
            }
        }

        return false;
    }

    @Transactional(readOnly = true)
    public List<RoleDTO> getAssignableRoles(MyUserDetails currentUserDetails) {
        User currentUser = currentUserDetails.getUser();

        List<Role> roles;

        if (isAdmin(currentUser)) {
            roles = roleRepository.findAll();
        } else if (isLead(currentUser)) {
            roles = roleRepository.findByDepartment(currentUser.getDepartment());
        } else {
            throw new RuntimeException("You are not allowed to assign roles.");
        }

        return roles.stream()
                .map(role -> mapper.map(role, RoleDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getDepartmentWorkers(MyUserDetails currentUserDetails) {
        User currentUser = currentUserDetails.getUser();

        List<User> users;

        if (isAdmin(currentUser)) {
            users = userRepository.findAll();
        } else if (isLead(currentUser)) {
            users = userRepository.findByDepartment(currentUser.getDepartment());
        } else {
            throw new RuntimeException("You are not allowed to view department workers.");
        }

        return users.stream()
                .map(user -> mapper.map(user, UserDTO.class))
                .collect(Collectors.toList());
    }

    public void assignRoleToUser(MyUserDetails currentUserDetails, Long targetUserId, Long roleId) {
        User currentUser = currentUserDetails.getUser();

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (isAdmin(currentUser)) {
            addRoleIfMissing(targetUser, role);
            return;
        }

        if (!isLead(currentUser)) {
            throw new RuntimeException("You are not allowed to assign roles.");
        }

        if (!currentUser.getDepartment().equals(role.getDepartment())) {
            throw new RuntimeException("You cannot assign a role outside your department.");
        }

        if (!currentUser.getDepartment().equals(targetUser.getDepartment())) {
            throw new RuntimeException("You cannot assign a role to a user outside your department.");
        }

        addRoleIfMissing(targetUser, role);
    }

    public void removeRoleFromUser(MyUserDetails currentUserDetails, Long targetUserId, Long roleId) {
        User currentUser = currentUserDetails.getUser();

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (isAdmin(currentUser)) {
            targetUser.getRoles().remove(role);
            userRepository.save(targetUser);
            return;
        }

        if (!isLead(currentUser)) {
            throw new RuntimeException("You are not allowed to remove roles.");
        }

        if (!currentUser.getDepartment().equals(role.getDepartment())) {
            throw new RuntimeException("You cannot remove a role outside your department.");
        }

        if (!currentUser.getDepartment().equals(targetUser.getDepartment())) {
            throw new RuntimeException("You cannot remove a role from a user outside your department.");
        }

        targetUser.getRoles().remove(role);
        userRepository.save(targetUser);
    }

    public void grantAccessToRole(MyUserDetails currentUserDetails, AccessRightsDTO accessRightsDTO) {
        User currentUser = currentUserDetails.getUser();

        Entry entry = entryRepository.findById(accessRightsDTO.getEntryId())
                .orElseThrow(() -> new RuntimeException("Entry not found"));

        Role role = roleRepository.findById(accessRightsDTO.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (isAdmin(currentUser)) {
            saveOrUpdateRoleAccess(role, entry, accessRightsDTO.isCanView(), accessRightsDTO.isCanEdit());
            return;
        }

        if (!isLead(currentUser)) {
            throw new RuntimeException("You are not allowed to grant access to roles.");
        }

        if (!currentUser.getDepartment().equals(role.getDepartment())) {
            throw new RuntimeException("You cannot manage access for roles outside your department.");
        }

        if (!hasAccess(entry.getId(), currentUser)) {
            throw new RuntimeException("You cannot grant access to an entry you do not have access to.");
        }

        saveOrUpdateRoleAccess(role, entry, accessRightsDTO.isCanView(), accessRightsDTO.isCanEdit());
    }

    public void grantAccessToUser(MyUserDetails currentUserDetails, UserAccessRightsDTO userAccessRightsDTO) {
        User currentUser = currentUserDetails.getUser();

        User targetUser = userRepository.findById(userAccessRightsDTO.getTargetUserId())
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        Entry entry = entryRepository.findById(userAccessRightsDTO.getEntryId())
                .orElseThrow(() -> new RuntimeException("Entry not found"));

        if (isAdmin(currentUser)) {
            saveOrUpdateUserAccess(targetUser, entry, userAccessRightsDTO.isCanView(), userAccessRightsDTO.isCanEdit());
            return;
        }

        if (!isLead(currentUser)) {
            throw new RuntimeException("You are not allowed to grant personal access.");
        }

        if (!currentUser.getDepartment().equals(targetUser.getDepartment())) {
            throw new RuntimeException("You cannot manage users outside your department.");
        }

        if (!hasAccess(entry.getId(), currentUser)) {
            throw new RuntimeException("You cannot grant access to an entry you do not have access to.");
        }

        saveOrUpdateUserAccess(targetUser, entry, userAccessRightsDTO.isCanView(), userAccessRightsDTO.isCanEdit());
    }

    public void revokeAccessFromUser(MyUserDetails currentUserDetails, Long targetUserId, Long entryId) {
        User currentUser = currentUserDetails.getUser();

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        Entry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Entry not found"));

        if (isAdmin(currentUser)) {
            userAccessRightsRepository.deleteByUserAndEntry(targetUser, entry);
            return;
        }

        if (!isLead(currentUser)) {
            throw new RuntimeException("You are not allowed to revoke personal access.");
        }

        if (!currentUser.getDepartment().equals(targetUser.getDepartment())) {
            throw new RuntimeException("You cannot manage users outside your department.");
        }

        if (!hasAccess(entry.getId(), currentUser)) {
            throw new RuntimeException("You cannot revoke access to an entry you do not have access to.");
        }

        userAccessRightsRepository.deleteByUserAndEntry(targetUser, entry);
    }

    private void saveOrUpdateRoleAccess(Role role, Entry entry, boolean canView, boolean canEdit) {
        AccessRights accessRights = accessRightsRepository.findByRoleAndEntry(role, entry)
                .orElseGet(AccessRights::new);

        accessRights.setRole(role);
        accessRights.setEntry(entry);
        accessRights.setCanView(canView);
        accessRights.setCanEdit(canEdit);

        accessRightsRepository.save(accessRights);
    }

    private void saveOrUpdateUserAccess(User user, Entry entry, boolean canView, boolean canEdit) {
        UserAccessRights userAccessRights = userAccessRightsRepository.findByUserAndEntry(user, entry)
                .orElseGet(UserAccessRights::new);

        userAccessRights.setUser(user);
        userAccessRights.setEntry(entry);
        userAccessRights.setCanView(canView);
        userAccessRights.setCanEdit(canEdit);

        userAccessRightsRepository.save(userAccessRights);
    }

    private void addRoleIfMissing(User targetUser, Role role) {
        if (!targetUser.getRoles().contains(role)) {
            targetUser.getRoles().add(role);
            userRepository.save(targetUser);
        }
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> "ROLE_ADMIN".equalsIgnoreCase(role.getName()));
    }

    private boolean isLead(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> "ROLE_LEAD".equalsIgnoreCase(role.getName()));
    }
}