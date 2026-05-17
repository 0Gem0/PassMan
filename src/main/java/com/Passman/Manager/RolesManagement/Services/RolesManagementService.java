package com.Passman.Manager.RolesManagement.Services;

import com.Passman.Manager.Auth.Models.User;
import com.Passman.Manager.Auth.Repos.UserRepository;
import com.Passman.Manager.Auth.Security.MyUserDetails;
import com.Passman.Manager.RolesManagement.DTO.*;
import com.Passman.Manager.RolesManagement.Models.*;
import com.Passman.Manager.RolesManagement.Repos.*;
import com.Passman.Manager.Vault.DTO.EntryDTO;
import com.Passman.Manager.Vault.DTO.EntryRoleAccessDTO;
import com.Passman.Manager.Vault.DTO.EntryUserAccessDTO;
import com.Passman.Manager.Vault.Models.Entry;
import com.Passman.Manager.Vault.Repos.EntryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class RolesManagementService {

    private final UserAccessRightsRepository userAccessRightsRepository;

    private final EntryKeyRepository entryKeyRepository;

    private final DepartmentRepository departmentRepository;

    private final AccessRightsRepository accessRightsRepository;
    private final UserRepository userRepository;
    private final ModelMapper mapper;
    private final EntryRepository entryRepository;
    private final RoleRepository roleRepository;

    public RolesManagementService(UserAccessRightsRepository userAccessRightsRepository, EntryKeyRepository entryKeyRepository, DepartmentRepository departmentRepository,
                                  AccessRightsRepository accessRightsRepository,
                                  UserRepository userRepository, ModelMapper mapper,
                                  EntryRepository entryRepository,
                                  RoleRepository roleRepository) {
        this.userAccessRightsRepository = userAccessRightsRepository;
        this.entryKeyRepository = entryKeyRepository;
        this.departmentRepository = departmentRepository;
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

    public List<EntryDTO> findAllAccessibleAsDtoShow(User currentUser) {
        List<Entry> entries;

        if (isAdmin(currentUser)) {
            entries = entryRepository.findAll();
        } else {
            entries = entryRepository.findAccessibleEntries(currentUser.getId());
        }

        return entries.stream()
                .filter(entry -> entryKeyRepository
                        .existsByEntryIdAndUserId(entry.getId(), currentUser.getId()))
                .map(entry -> toEntryDTO(entry, currentUser))
                .collect(Collectors.toList());
    }

    public EntryDTO toEntryDTO(Entry entry, User user) {
        EntryKey entryKey = entryKeyRepository
                .findByEntryIdAndUserId(entry.getId(), user.getId())
                .orElseThrow(() -> new RuntimeException(
                        "EntryKey not found for entryId=" + entry.getId() +
                                ", userId=" + user.getId()
                ));

        EntryDTO dto = new EntryDTO();

        mapper.map(entry, dto);

        dto.setEncryptedDek(entryKey.getEncryptedDek());
        dto.setDekIv(entryKey.getDekIv());
        dto.setDekEnvelopeType(entryKey.getDekEnvelopeType());

        dto.setRoleAccesses(
                accessRightsRepository.findAllByEntryId(entry.getId())
                        .stream()
                        .map(ar -> new EntryRoleAccessDTO(
                                ar.getRole().getId(),
                                ar.getRole().getName(),
                                ar.isCanView(),
                                ar.isCanEdit()
                        ))
                        .toList()
        );

        dto.setUserAccesses(
                userAccessRightsRepository.findAllByEntryId(entry.getId())
                        .stream()
                        .map(uar -> new EntryUserAccessDTO(
                                uar.getUser().getId(),
                                uar.getUser().getLogin(),
                                uar.isCanView(),
                                uar.isCanEdit()
                        ))
                        .toList()
        );

        return dto;
    }

    public boolean[] resolveEntryPermissions(Entry entry, User currentUser) {
        Long entryId = entry.getId();

        boolean isOwner = entry.getUser() != null
                && entry.getUser().getId().equals(currentUser.getId());

        if (isOwner) {
            return new boolean[]{true, true};
        }

        Optional<UserAccessRights> personalRights =
                userAccessRightsRepository.findByUserAndEntryId(currentUser, entryId);

        if (personalRights.isPresent()) {
            UserAccessRights rights = personalRights.get();
            return new boolean[]{
                    rights.isCanView() || rights.isCanEdit(),
                    rights.isCanEdit()
            };
        }

        boolean canView = false;
        boolean canEdit = false;

        for (Role role : currentUser.getRoles()) {
            Optional<AccessRights> roleRights =
                    accessRightsRepository.findByRoleAndEntryId(role, entryId);

            if (roleRights.isPresent()) {
                AccessRights rights = roleRights.get();

                if (rights.isCanView()) {
                    canView = true;
                }

                if (rights.isCanEdit()) {
                    canEdit = true;
                    canView = true;
                }
            }
        }

        return new boolean[]{canView, canEdit};
    }
    @Transactional(readOnly = true)
    public UserPublicKeyDTO getUserPublicKey(MyUserDetails currentUserDetails, Long targetUserId) {
        User currentUser = currentUserDetails.getUser();

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        if (targetUser.getPublicKey() == null || targetUser.getPublicKey().isBlank()) {
            throw new RuntimeException("Target user does not have a public key");
        }

        if (isAdmin(currentUser)) {
            return new UserPublicKeyDTO(targetUser.getPublicKey());
        }

        if (!isLead(currentUser)) {
            throw new RuntimeException("You are not allowed to view public keys.");
        }

        if (!currentUser.getDepartment().getName().equals(targetUser.getDepartment().getName())) {
            throw new RuntimeException("You cannot access users outside your department.");
        }

        return new UserPublicKeyDTO(targetUser.getPublicKey());
    }

    @Transactional
    public void shareEntry(MyUserDetails currentUserDetails, ShareEntryDTO shareEntryDTO) {
        User currentUser = currentUserDetails.getUser();

        Entry entry = entryRepository.findById(shareEntryDTO.getEntryId())
                .orElseThrow(() -> new RuntimeException("Entry not found"));

        User targetUser = userRepository.findById(shareEntryDTO.getTargetUserId())
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        if (shareEntryDTO.getEncryptedDek() == null || shareEntryDTO.getEncryptedDek().isBlank()) {
            throw new RuntimeException("Encrypted DEK is required");
        }

        if (shareEntryDTO.getDekEnvelopeType() == null || shareEntryDTO.getDekEnvelopeType().isBlank()) {
            throw new RuntimeException("DEK envelope type is required");
        }

        /**
         * ADMIN может шарить любую запись,
         * LEAD — только в рамках доступной ему записи и пользователей своего отдела,
         * USER — нельзя
         */
        if (isAdmin(currentUser)) {
            saveOrUpdateEntryKey(entry, targetUser, shareEntryDTO.getDekEnvelopeType(), shareEntryDTO.getEncryptedDek(), null);
            return;
        }
        if (!isLead(currentUser)) {
            throw new RuntimeException("You are not allowed to share entries.");
        }
        if (!currentUser.getDepartment().getName().equals(targetUser.getDepartment().getName())) {
            throw new RuntimeException("You cannot share entries with users outside your department.");
        }
        if (!hasAccess(entry.getId(), currentUser)) {
            throw new RuntimeException("You cannot share an entry you do not have access to.");
        }
        saveOrUpdateEntryKey(entry, targetUser, shareEntryDTO.getDekEnvelopeType(), shareEntryDTO.getEncryptedDek(), null);
    }

    @Transactional
    protected void saveOrUpdateEntryKey(Entry entry,
                                        User targetUser,
                                        String dekEnvelopeType,
                                        String encryptedDek,
                                        String dekIv) {
        EntryKey entryKey = entryKeyRepository.findByEntryIdAndUserId(entry.getId(), targetUser.getId())
                .orElseGet(EntryKey::new);

        entryKey.setEntry(entry);
        entryKey.setUser(targetUser);
        entryKey.setDekEnvelopeType(dekEnvelopeType);
        entryKey.setEncryptedDek(encryptedDek);
        entryKey.setDekIv(dekIv);

        entryKeyRepository.save(entryKey);
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

    @Transactional
    public void assignDepartment(MyUserDetails currentUserDetails, AssignDepartmentDTO dto) {
        User currentUser = currentUserDetails.getUser();

        User targetUser = userRepository.findById(dto.getTargetUserId())
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        if (isAdmin(currentUser)) {
            targetUser.setDepartment(department);
            userRepository.save(targetUser);
            return;
        }

        if (!isLead(currentUser)) {
            throw new RuntimeException("You are not allowed to assign departments");
        }

        if (currentUser.getDepartment() == null) {
            throw new RuntimeException("Current user has no department");
        }

        if (!currentUser.getDepartment().getId().equals(department.getId())) {
            throw new RuntimeException("Lead can assign only own department");
        }

        if (targetUser.getDepartment() != null &&
                !targetUser.getDepartment().getId().equals(currentUser.getDepartment().getId())) {
            throw new RuntimeException("Lead cannot move users from another department");
        }

        targetUser.setDepartment(department);
        userRepository.save(targetUser);
    }

    @Transactional(readOnly = true)
    public List<UserPublicKeyDTO> getDepartmentWorkers(MyUserDetails currentUserDetails) {
        User currentUser = currentUserDetails.getUser();

        List<User> users;

        if (isAdmin(currentUser)) {
            users = userRepository.findAll();
        } else if (isLead(currentUser)) {
            users = userRepository.findByDepartmentId(currentUser.getDepartment().getId());
        } else {
            throw new RuntimeException("You are not allowed to view department workers.");
        }

        return users.stream()
                .map(user -> mapper.map(user, UserPublicKeyDTO.class))
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
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Роль не найдена"
                ));

        if (!targetUser.getRoles().contains(role)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "У пользователя нет выбранной роли"
            );
        }

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
        System.out.println(currentUser.getDepartment().getName());
        System.out.println(targetUser.getDepartment().getName());
        if (!currentUser.getDepartment().getId().equals(targetUser.getDepartment().getId())) {
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

    @Transactional
    public void revokeAccessFromRole(MyUserDetails currentUserDetails, Long roleId, Long entryId) {
        User currentUser = currentUserDetails.getUser();

        Entry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Entry not found"));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        AccessRights accessRights = accessRightsRepository
                .findByRoleIdAndEntryId(roleId, entryId)
                .orElseThrow(() -> new RuntimeException("Access rights not found"));

        checkCanManageRoleAccess(currentUser, role, entry);

        List<User> usersWithRole = userRepository.findUsersByRoleId(roleId);

        accessRightsRepository.delete(accessRights);

        for (User user : usersWithRole) {
            boolean shouldKeepKey = shouldUserKeepEntryKeyAfterRoleRevoke(user, entry);

            if (!shouldKeepKey) {
                entryKeyRepository.deleteByEntryIdAndUserId(entryId, user.getId());
            }
        }
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

    public List<UserPublicKeyDTO> findUsersByRole(Long roleId, Long userId){
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()){
            if (isAdmin(userOptional.get()) || isLead(userOptional.get())){
                Optional<Role> roleOptional = roleRepository.findById(roleId);
                if (roleOptional.isPresent()){
                    return roleOptional.get().getUsers().stream().map(user -> mapper.map(user, UserPublicKeyDTO.class)).collect(Collectors.toList());
                }
                else {
                    throw new RuntimeException("No role");
                }
            }
        }
        throw new RuntimeException("No user");
    }

    @Transactional(readOnly = true)
    public List<DepartmentDTO> getAssignableDepartments(MyUserDetails currentUserDetails) {
        User currentUser = currentUserDetails.getUser();

        if (isAdmin(currentUser)) {
            return departmentRepository.findAll()
                    .stream()
                    .map(dep -> new DepartmentDTO(dep.getId(), dep.getName()))
                    .toList();
        }

        if (isLead(currentUser)) {
            if (currentUser.getDepartment() == null) {
                throw new RuntimeException("Current user has no department");
            }

            return List.of(
                    new DepartmentDTO(
                            currentUser.getDepartment().getId(),
                            currentUser.getDepartment().getName()
                    )
            );
        }

        throw new RuntimeException("You are not allowed to assign departments");
    }

    private void checkCanManageRoleAccess(User currentUser, Role role, Entry entry) {
        if (isAdmin(currentUser)) {
            return;
        }

        if (!isLead(currentUser)) {
            throw new RuntimeException("You are not allowed to manage role access");
        }

        if (currentUser.getDepartment() == null) {
            throw new RuntimeException("Current user has no department");
        }

        if (role.getDepartment() == null) {
            throw new RuntimeException("Role has no department");
        }

        if (!currentUser.getDepartment().getId().equals(role.getDepartment().getId())) {
            throw new RuntimeException("Lead can manage only roles from own department");
        }

        if (!entryKeyRepository.existsByEntryIdAndUserId(entry.getId(), currentUser.getId())) {
            throw new RuntimeException("Current user has no cryptographic access to this entry");
        }
    }


    private boolean shouldUserKeepEntryKeyAfterRoleRevoke(User user, Entry entry) {
        Long userId = user.getId();
        Long entryId = entry.getId();

        boolean isOwner = entry.getUser() != null &&
                entry.getUser().getId().equals(userId);

        if (isOwner) {
            return true;
        }

        boolean hasPersonalAccess = userAccessRightsRepository
                .existsEffectivePersonalAccess(userId, entryId);

        if (hasPersonalAccess) {
            return true;
        }

        boolean hasAccessThroughAnotherRole = accessRightsRepository
                .existsEffectiveRoleAccessForUser(userId, entryId);

        return hasAccessThroughAnotherRole;
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

    @Transactional
    public void createRole(MyUserDetails currentUserDetails, CreateRoleDTO dto) {
        User currentUser = currentUserDetails.getUser();

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new RuntimeException("Role name is required");
        }

        String roleName = dto.getName().trim();

        Department department;

        if (isAdmin(currentUser)) {
            if (dto.getDepartmentId() == null) {
                throw new RuntimeException("Department is required for admin role creation");
            }

            department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
        } else if (isLead(currentUser)) {
            if (currentUser.getDepartment() == null) {
                throw new RuntimeException("Lead has no department");
            }

            department = currentUser.getDepartment();
        } else {
            throw new RuntimeException("You are not allowed to create roles");
        }

        if (roleRepository.existsByNameAndDepartmentId(roleName, department.getId())) {
            throw new RuntimeException("Role already exists in this department");
        }

        Role role = new Role();
        role.setName(roleName);
        role.setDepartment(department);

        roleRepository.save(role);
    }
}