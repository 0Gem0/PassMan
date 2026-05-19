package com.Passman.Manager.management_roles.internal.Services;

import com.Passman.Manager.auth.AuthApi;
import com.Passman.Manager.auth.UserView;
import com.Passman.Manager.auth.internal.Models.User;
import com.Passman.Manager.shared.Security.MyUserDetails;
import com.Passman.Manager.management_roles.DTO.*;
import com.Passman.Manager.management_roles.internal.Models.*;
import com.Passman.Manager.management_roles.internal.Repos.*;
import com.Passman.Manager.vault.DTO.EntryDTO;
import com.Passman.Manager.vault.EntryView;
import com.Passman.Manager.vault.VaultApi;
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
    private final ModelMapper mapper;
    private final RoleRepository roleRepository;
    private final UsersRolesRepository usersRolesRepository;

    private final VaultApi vaultApi;

    private final AuthApi authApi;

    public RolesManagementService(UserAccessRightsRepository userAccessRightsRepository, EntryKeyRepository entryKeyRepository, DepartmentRepository departmentRepository,
                                  AccessRightsRepository accessRightsRepository,
                                  ModelMapper mapper,
                                  RoleRepository roleRepository, UsersRolesRepository usersRolesRepository, VaultApi vaultApi, AuthApi authApi) {
        this.userAccessRightsRepository = userAccessRightsRepository;
        this.entryKeyRepository = entryKeyRepository;
        this.departmentRepository = departmentRepository;
        this.accessRightsRepository = accessRightsRepository;
        this.mapper = mapper;
        this.roleRepository = roleRepository;
        this.usersRolesRepository = usersRolesRepository;
        this.vaultApi = vaultApi;
        this.authApi = authApi;
    }

    public boolean hasAccess(Long entryId, Long userId) {
        var userAccessRightsOptional = userAccessRightsRepository.findByUserIdAndEntryId(userId, entryId);

        if (userAccessRightsOptional.isPresent()) {
            UserAccessRights userAccessRights = userAccessRightsOptional.get();
            return userAccessRights.isCanView() || userAccessRights.isCanEdit();
        }


        for (Long roleId : usersRolesRepository.findAllByUserId(userId)) {
            var accessRightsOptional = accessRightsRepository.findByRoleIdAndEntryId(roleId, entryId);
            if (accessRightsOptional.isPresent()) {
                AccessRights accessRights = accessRightsOptional.get();
                if (accessRights.isCanView() || accessRights.isCanEdit()) {
                    return true;
                }
            }
        }

        return false;
    }

    public List<EntryRolesDTO> findAllAccessibleAsDtoShow(User currentUser) {
        List<EntryView> entries;

        if (isAdmin(currentUser.getId())) {
            entries = vaultApi.findAll();
        } else {
            entries = vaultApi.findAccessibleEntries(currentUser.getId());
        }

        return entries.stream()
                .filter(entry -> entryKeyRepository
                        .existsByEntryIdAndUserId(entry.id(), currentUser.getId()))
                .map(entry -> toEntryDTO(entry, currentUser.getId()))
                .collect(Collectors.toList());
    }

    public EntryRolesDTO toEntryDTO(EntryView entry, Long userId) {
        EntryKey entryKey = entryKeyRepository
                .findByEntryIdAndUserId(entry.id(), userId)
                .orElseThrow(() -> new RuntimeException(
                        "EntryKey not found for entryId=" + entry.id() +
                                ", userId=" + userId
                ));

        EntryRolesDTO dto = new EntryRolesDTO();

        mapper.map(entry, dto);

        dto.setEncryptedDek(entryKey.getEncryptedDek());
        dto.setDekIv(entryKey.getDekIv());
        dto.setDekEnvelopeType(entryKey.getDekEnvelopeType());

        dto.setRoleAccesses(
                accessRightsRepository.findAllByEntryId(entry.id())
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
                userAccessRightsRepository.findAllByEntryId(entry.id())
                        .stream()
                        .map(uar -> new EntryUserAccessDTO(
                                uar.getUserId(),
                                uar.isCanView(),
                                uar.isCanEdit()
                        ))
                        .toList()
        );

        return dto;
    }

    @Transactional(readOnly = true)
    public boolean[] resolveEntryPermissions(EntryView entry, Long currentUserId) {
        Long entryId = entry.id();

        boolean isOwner = entry.userId() != null
                && entry.userId().equals(currentUserId);

        if (isOwner) {
            return new boolean[]{true, true};
        }

        Optional<UserAccessRights> personalRights =
                userAccessRightsRepository.findByUserIdAndEntryId(currentUserId, entryId);

        if (personalRights.isPresent()) {
            UserAccessRights rights = personalRights.get();

            return new boolean[]{
                    rights.isCanView() || rights.isCanEdit(),
                    rights.isCanEdit()
            };
        }

        boolean canView = false;
        boolean canEdit = false;

        List<Long> roleIds = usersRolesRepository.findAllByUserId(currentUserId);

        for (Long roleId : roleIds) {
            Optional<AccessRights> roleRights =
                    accessRightsRepository.findByRoleIdAndEntryId(roleId, entryId);

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
    public UserPublicKeyDTO getUserPublicKey(Long currentUserId, Long targetUserId) {
        UserView currentUser = authApi.getUserView(currentUserId);
        UserView targetUser = authApi.getUserView(targetUserId);

        if (targetUser.publicKey() == null || targetUser.publicKey().isBlank()) {
            throw new RuntimeException("Target user does not have a public key");
        }

        if (isAdmin(currentUserId)) {
            return new UserPublicKeyDTO(targetUser.publicKey());
        }

        if (!isLead(currentUserId)) {
            throw new RuntimeException("You are not allowed to view public keys.");
        }

        if (currentUser.departmentId() == null || targetUser.departmentId() == null) {
            throw new RuntimeException("Department is not defined.");
        }

        if (!currentUser.departmentId().equals(targetUser.departmentId())) {
            throw new RuntimeException("You cannot access users outside your department.");
        }

        return new UserPublicKeyDTO(targetUser.publicKey());
    }

    @Transactional
    public void shareEntry(Long currentUserId, ShareEntryDTO shareEntryDTO) {
        Long entryId = shareEntryDTO.getEntryId();
        Long targetUserId = shareEntryDTO.getTargetUserId();

        if (entryId == null) {
            throw new RuntimeException("Entry id is required");
        }

        if (targetUserId == null) {
            throw new RuntimeException("Target user id is required");
        }

        if (!vaultApi.entryExists(entryId)) {
            throw new RuntimeException("Entry not found");
        }

        UserView currentUser = authApi.getUserView(currentUserId);
        UserView targetUser = authApi.getUserView(targetUserId);

        if (shareEntryDTO.getEncryptedDek() == null || shareEntryDTO.getEncryptedDek().isBlank()) {
            throw new RuntimeException("Encrypted DEK is required");
        }

        if (shareEntryDTO.getDekEnvelopeType() == null || shareEntryDTO.getDekEnvelopeType().isBlank()) {
            throw new RuntimeException("DEK envelope type is required");
        }

        if (isAdmin(currentUserId)) {
            saveOrUpdateEntryKey(
                    entryId,
                    targetUserId,
                    shareEntryDTO.getDekEnvelopeType(),
                    shareEntryDTO.getEncryptedDek(),
                    null
            );
            return;
        }

        if (!isLead(currentUserId)) {
            throw new RuntimeException("You are not allowed to share entries.");
        }

        if (currentUser.departmentId() == null || targetUser.departmentId() == null) {
            throw new RuntimeException("Department is not defined.");
        }

        if (!currentUser.departmentId().equals(targetUser.departmentId())) {
            throw new RuntimeException("You cannot share entries with users outside your department.");
        }

        if (!hasAccess(entryId, currentUserId)) {
            throw new RuntimeException("You cannot share an entry you do not have access to.");
        }

        saveOrUpdateEntryKey(
                entryId,
                targetUserId,
                shareEntryDTO.getDekEnvelopeType(),
                shareEntryDTO.getEncryptedDek(),
                null
        );
    }
    private void saveOrUpdateEntryKey(
            Long entryId,
            Long userId,
            String dekEnvelopeType,
            String encryptedDek,
            String dekIv
    ) {
        EntryKey entryKey = entryKeyRepository
                .findByEntryIdAndUserId(entryId, userId)
                .orElseGet(EntryKey::new);

        entryKey.setEntryId(entryId);
        entryKey.setUserId(userId);
        entryKey.setDekEnvelopeType(dekEnvelopeType);
        entryKey.setEncryptedDek(encryptedDek);
        entryKey.setDekIv(dekIv);

        entryKeyRepository.save(entryKey);
    }

    public boolean hasEditAccess(Long entryId, User user) {
        var userAccessRightsOptional = userAccessRightsRepository.findByUserIdAndEntryId(user.getId(), entryId);

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

        if (isAdmin(currentUser.getId())) {
            roles = roleRepository.findAll();
        } else if (isLead(currentUser.getId())) {
            roles = roleRepository.findByDepartmentId(currentUser.getDepartmentId());
        } else {
            throw new RuntimeException("You are not allowed to assign roles.");
        }

        return roles.stream()
                .map(role -> mapper.map(role, RoleDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public void assignDepartment(Long currentUserId, AssignDepartmentDTO dto) {
        if (dto.getTargetUserId() == null) {
            throw new RuntimeException("Target user id is required");
        }

        if (dto.getDepartmentId() == null) {
            throw new RuntimeException("Department id is required");
        }

        UserView currentUser = authApi.getUserView(currentUserId);
        UserView targetUser = authApi.getUserView(dto.getTargetUserId());

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        if (isAdmin(currentUserId)) {
            authApi.updateUserDepartment(targetUser.id(), department.getId());
            return;
        }

        if (!isLead(currentUserId)) {
            throw new RuntimeException("You are not allowed to assign departments");
        }

        if (currentUser.departmentId() == null) {
            throw new RuntimeException("Current user has no department");
        }

        if (!currentUser.departmentId().equals(department.getId())) {
            throw new RuntimeException("Lead can assign only own department");
        }

        if (targetUser.departmentId() != null
                && !targetUser.departmentId().equals(currentUser.departmentId())) {
            throw new RuntimeException("Lead cannot move users from another department");
        }

        authApi.updateUserDepartment(targetUser.id(), department.getId());
    }

    @Transactional(readOnly = true)
    public List<UserPublicKeyDTO> getDepartmentWorkers(Long currentUserId) {
        UserView currentUser = authApi.getUserView(currentUserId);

        List<UserView> users;

        if (isAdmin(currentUserId)) {
            users = authApi.findAllUsers();
        } else if (isLead(currentUserId)) {
            if (currentUser.departmentId() == null) {
                throw new RuntimeException("Current user has no department");
            }

            users = authApi.findUsersByDepartmentId(currentUser.departmentId());
        } else {
            throw new RuntimeException("You are not allowed to view department workers.");
        }

        return users.stream()
                .map(this::toUserPublicKeyDTO)
                .toList();
    }
    private UserPublicKeyDTO toUserPublicKeyDTO(UserView user) {
        UserPublicKeyDTO dto = new UserPublicKeyDTO();

        dto.setId(user.id());
        dto.setLogin(user.login());
        dto.setPublicKey(user.publicKey());

        dto.setDepartmentId(user.departmentId());
        dto.setDepartmentName(resolveDepartmentName(user.departmentId()));

        dto.setRoles(findUserRoles(user.id()));

        return dto;
    }

    private String resolveDepartmentName(Long departmentId) {
        if (departmentId == null) {
            return null;
        }

        return departmentRepository.findById(departmentId)
                .map(Department::getName)
                .orElse(null);
    }

    private List<RoleDTO> findUserRoles(Long userId) {
        List<Long> roleIds = usersRolesRepository.findAllByUserId(userId);

        if (roleIds.isEmpty()) {
            return List.of();
        }

        return roleRepository.findAllById(roleIds)
                .stream()
                .map(this::toRoleDTO)
                .toList();
    }

    private RoleDTO toRoleDTO(Role role) {
        RoleDTO dto = new RoleDTO();

        dto.setId(role.getId());
        dto.setName(role.getName());

        return dto;
    }
    @Transactional
    public void assignRoleToUser(Long currentUserId, Long targetUserId, Long roleId) {
        if (targetUserId == null) {
            throw new RuntimeException("Target user id is required");
        }

        if (roleId == null) {
            throw new RuntimeException("Role id is required");
        }

        UserView currentUser = authApi.getUserView(currentUserId);
        UserView targetUser = authApi.getUserView(targetUserId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (isAdmin(currentUserId)) {
            addRoleIfMissing(targetUserId, roleId);
            return;
        }

        if (!isLead(currentUserId)) {
            throw new RuntimeException("You are not allowed to assign roles.");
        }

        if (currentUser.departmentId() == null) {
            throw new RuntimeException("Current user has no department.");
        }

        if (role.getDepartment() == null) {
            throw new RuntimeException("Role has no department.");
        }

        if (!currentUser.departmentId().equals(role.getDepartment().getId())) {
            throw new RuntimeException("You cannot assign a role outside your department.");
        }

        if (targetUser.departmentId() == null
                || !currentUser.departmentId().equals(targetUser.departmentId())) {
            throw new RuntimeException("You cannot assign a role to a user outside your department.");
        }

        addRoleIfMissing(targetUserId, roleId);
    }

    @Transactional
    public void removeRoleFromUser(Long currentUserId, Long targetUserId, Long roleId) {
        if (targetUserId == null) {
            throw new RuntimeException("Target user id is required");
        }

        if (roleId == null) {
            throw new RuntimeException("Role id is required");
        }

        UserView currentUser = authApi.getUserView(currentUserId);
        UserView targetUser = authApi.getUserView(targetUserId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Роль не найдена"
                ));

        if (!usersRolesRepository.existsByUserIdAndRoleId(targetUserId, roleId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "У пользователя нет выбранной роли"
            );
        }

        if (isAdmin(currentUserId)) {
            usersRolesRepository.deleteByUserIdAndRoleId(targetUserId, roleId);
            return;
        }

        if (!isLead(currentUserId)) {
            throw new RuntimeException("You are not allowed to remove roles.");
        }

        if (currentUser.departmentId() == null) {
            throw new RuntimeException("Current user has no department.");
        }

        if (targetUser.departmentId() == null) {
            throw new RuntimeException("Target user has no department.");
        }

        if (role.getDepartment() == null) {
            throw new RuntimeException("Role has no department.");
        }

        if (!currentUser.departmentId().equals(role.getDepartment().getId())) {
            throw new RuntimeException("You cannot remove a role outside your department.");
        }

        if (!currentUser.departmentId().equals(targetUser.departmentId())) {
            throw new RuntimeException("You cannot remove a role from a user outside your department.");
        }

        usersRolesRepository.deleteByUserIdAndRoleId(targetUserId, roleId);
    }
    @Transactional
    public void grantAccessToRole(Long currentUserId, AccessRightsDTO accessRightsDTO) {
        if (accessRightsDTO.getEntryId() == null) {
            throw new RuntimeException("Entry id is required");
        }

        if (accessRightsDTO.getRoleId() == null) {
            throw new RuntimeException("Role id is required");
        }

        Long entryId = accessRightsDTO.getEntryId();
        Long roleId = accessRightsDTO.getRoleId();

        UserView currentUser = authApi.getUserView(currentUserId);

        if (!vaultApi.entryExists(entryId)) {
            throw new RuntimeException("Entry not found");
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (isAdmin(currentUserId)) {
            saveOrUpdateRoleAccess(
                    role,
                    entryId,
                    accessRightsDTO.isCanView(),
                    accessRightsDTO.isCanEdit()
            );
            return;
        }

        if (!isLead(currentUserId)) {
            throw new RuntimeException("You are not allowed to grant access to roles.");
        }

        if (currentUser.departmentId() == null) {
            throw new RuntimeException("Current user has no department.");
        }

        if (role.getDepartment() == null) {
            throw new RuntimeException("Role has no department.");
        }

        if (!currentUser.departmentId().equals(role.getDepartment().getId())) {
            throw new RuntimeException("You cannot manage access for roles outside your department.");
        }

        if (!hasAccess(entryId, currentUserId)) {
            throw new RuntimeException("You cannot grant access to an entry you do not have access to.");
        }

        saveOrUpdateRoleAccess(
                role,
                entryId,
                accessRightsDTO.isCanView(),
                accessRightsDTO.isCanEdit()
        );
    }
    @Transactional
    public void grantAccessToUser(Long currentUserId, UserAccessRightsDTO userAccessRightsDTO) {
        if (userAccessRightsDTO.getTargetUserId() == null) {
            throw new RuntimeException("Target user id is required");
        }

        if (userAccessRightsDTO.getEntryId() == null) {
            throw new RuntimeException("Entry id is required");
        }

        Long targetUserId = userAccessRightsDTO.getTargetUserId();
        Long entryId = userAccessRightsDTO.getEntryId();

        UserView currentUser = authApi.getUserView(currentUserId);
        UserView targetUser = authApi.getUserView(targetUserId);

        if (!vaultApi.entryExists(entryId)) {
            throw new RuntimeException("Entry not found");
        }

        if (isAdmin(currentUserId)) {
            saveOrUpdateUserAccess(
                    targetUserId,
                    entryId,
                    userAccessRightsDTO.isCanView(),
                    userAccessRightsDTO.isCanEdit()
            );
            return;
        }

        if (!isLead(currentUserId)) {
            throw new RuntimeException("You are not allowed to grant personal access.");
        }

        if (currentUser.departmentId() == null) {
            throw new RuntimeException("Current user has no department.");
        }

        if (targetUser.departmentId() == null) {
            throw new RuntimeException("Target user has no department.");
        }

        if (!currentUser.departmentId().equals(targetUser.departmentId())) {
            throw new RuntimeException("You cannot manage users outside your department.");
        }

        if (!hasAccess(entryId, currentUserId)) {
            throw new RuntimeException("You cannot grant access to an entry you do not have access to.");
        }

        saveOrUpdateUserAccess(
                targetUserId,
                entryId,
                userAccessRightsDTO.isCanView(),
                userAccessRightsDTO.isCanEdit()
        );
    }
    @Transactional
    public void revokeAccessFromUser(Long currentUserId, Long targetUserId, Long entryId) {
        if (targetUserId == null) {
            throw new RuntimeException("Target user id is required");
        }

        if (entryId == null) {
            throw new RuntimeException("Entry id is required");
        }

        UserView currentUser = authApi.getUserView(currentUserId);
        UserView targetUser = authApi.getUserView(targetUserId);

        EntryView entry = vaultApi.getEntryView(entryId);

        if (isAdmin(currentUserId)) {
            userAccessRightsRepository.deleteByUserIdAndEntryId(targetUserId, entryId);
            deleteEntryKeyIfUserHasNoMoreAccess(entry, targetUserId);
            return;
        }

        if (!isLead(currentUserId)) {
            throw new RuntimeException("You are not allowed to revoke personal access.");
        }

        if (currentUser.departmentId() == null) {
            throw new RuntimeException("Current user has no department.");
        }

        if (targetUser.departmentId() == null) {
            throw new RuntimeException("Target user has no department.");
        }

        if (!currentUser.departmentId().equals(targetUser.departmentId())) {
            throw new RuntimeException("You cannot manage users outside your department.");
        }

        if (!hasAccess(entryId, currentUserId)) {
            throw new RuntimeException("You cannot revoke access to an entry you do not have access to.");
        }

        userAccessRightsRepository.deleteByUserIdAndEntryId(targetUserId, entryId);
        deleteEntryKeyIfUserHasNoMoreAccess(entry, targetUserId);
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

    private void saveOrUpdateRoleAccess(
            Role role,
            Long entryId,
            boolean canView,
            boolean canEdit
    ) {
        AccessRights accessRights = accessRightsRepository
                .findByRoleIdAndEntryId(role.getId(), entryId)
                .orElseGet(AccessRights::new);

        accessRights.setRole(role);
        accessRights.setEntryId(entryId);

        accessRights.setCanView(canView || canEdit);
        accessRights.setCanEdit(canEdit);

        accessRightsRepository.save(accessRights);
    }

    private void saveOrUpdateUserAccess(
            Long targetUserId,
            Long entryId,
            boolean canView,
            boolean canEdit
    ) {
        UserAccessRights accessRights = userAccessRightsRepository
                .findByUserIdAndEntryId(targetUserId, entryId)
                .orElseGet(UserAccessRights::new);

        accessRights.setUserId(targetUserId);
        accessRights.setEntryId(entryId);

        accessRights.setCanView(canView || canEdit);
        accessRights.setCanEdit(canEdit);

        userAccessRightsRepository.save(accessRights);
    }

    private void deleteEntryKeyIfUserHasNoMoreAccess(EntryView entry, Long targetUserId) {
        if (entry.userId() != null && entry.userId().equals(targetUserId)) {
            return;
        }

        if (hasAccess(entry.id(), targetUserId)) {
            return;
        }

        entryKeyRepository.deleteByEntryIdAndUserId(entry.id(), targetUserId);
    }
    @Transactional(readOnly = true)
    public List<UserPublicKeyDTO> findUsersByRole(Long roleId, Long currentUserId) {
        if (roleId == null) {
            throw new RuntimeException("Role id is required");
        }

        UserView currentUser = authApi.getUserView(currentUserId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("No role"));

        if (isAdmin(currentUserId)) {
            return usersRolesRepository.findUserIdsByRoleId(roleId)
                    .stream()
                    .map(authApi::getUserView)
                    .map(this::toUserPublicKeyDTO)
                    .toList();
        }

        if (!isLead(currentUserId)) {
            throw new RuntimeException("No user");
        }

        if (currentUser.departmentId() == null) {
            throw new RuntimeException("Current user has no department");
        }

        if (role.getDepartment() == null ||
                !currentUser.departmentId().equals(role.getDepartment().getId())) {
            throw new RuntimeException("Lead can view only users from own department role");
        }

        return usersRolesRepository.findUserIdsByRoleId(roleId)
                .stream()
                .map(authApi::getUserView)
                .filter(user -> currentUser.departmentId().equals(user.departmentId()))
                .map(this::toUserPublicKeyDTO)
                .toList();
    }
    @Transactional(readOnly = true)
    public List<DepartmentDTO> getAssignableDepartments(Long currentUserId) {
        UserView currentUser = authApi.getUserView(currentUserId);

        if (isAdmin(currentUserId)) {
            return departmentRepository.findAll()
                    .stream()
                    .map(dep -> new DepartmentDTO(dep.getId(), dep.getName()))
                    .toList();
        }

        if (isLead(currentUserId)) {
            if (currentUser.departmentId() == null) {
                throw new RuntimeException("Current user has no department");
            }

            Department department = departmentRepository.findById(currentUser.departmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));

            return List.of(
                    new DepartmentDTO(
                            department.getId(),
                            department.getName()
                    )
            );
        }

        throw new RuntimeException("You are not allowed to assign departments");
    }
    private void checkCanManageRoleAccess(Long currentUserId, Role role, EntryView entry) {
        UserView currentUser = authApi.getUserView(currentUserId);

        if (isAdmin(currentUserId)) {
            return;
        }

        if (!isLead(currentUserId)) {
            throw new RuntimeException("You are not allowed to manage role access");
        }

        if (currentUser.departmentId() == null) {
            throw new RuntimeException("Current user has no department");
        }

        if (role.getDepartment() == null) {
            throw new RuntimeException("Role has no department");
        }

        if (!currentUser.departmentId().equals(role.getDepartment().getId())) {
            throw new RuntimeException("Lead can manage only roles from own department");
        }

        if (!entryKeyRepository.existsByEntryIdAndUserId(entry.id(), currentUserId)) {
            throw new RuntimeException("Current user has no cryptographic access to this entry");
        }
    }

    private boolean shouldUserKeepEntryKeyAfterRoleRevoke(Long userId, EntryView entry) {
        Long entryId = entry.id();

        boolean isOwner = entry.userId() != null &&
                entry.userId().equals(userId);

        if (isOwner) {
            return true;
        }

        boolean hasPersonalAccess = userAccessRightsRepository
                .existsEffectivePersonalAccess(userId, entryId);

        if (hasPersonalAccess) {
            return true;
        }

        return accessRightsRepository.existsEffectiveRoleAccessForUser(userId, entryId);
    }
    private void addRoleIfMissing(Long userId, Long roleId) {
        if (usersRolesRepository.existsByUserIdAndRoleId(userId, roleId)) {
            return;
        }

        UsersRoles usersRoles = new UsersRoles();
        usersRoles.setUserId(userId);
        usersRoles.setRoleId(roleId);

        usersRolesRepository.save(usersRoles);
    }

    @Transactional(readOnly = true)
    protected boolean isAdmin(Long userId) {
        return usersRolesRepository.existsByUserIdAndRoleNameIgnoreCase(
                userId,
                "ROLE_ADMIN"
        );
    }

    @Transactional(readOnly = true)
    protected boolean isLead(Long userId) {
        return usersRolesRepository.existsByUserIdAndRoleNameIgnoreCase(
                userId,
                "ROLE_LEAD"
        );
    }

    @Transactional
    public void createRole(Long currentUserId, CreateRoleDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new RuntimeException("Role name is required");
        }

        String roleName = dto.getName().trim();

        UserView currentUser = authApi.getUserView(currentUserId);

        Department department;

        if (isAdmin(currentUserId)) {
            if (dto.getDepartmentId() == null) {
                throw new RuntimeException("Department is required for admin role creation");
            }

            department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
        } else if (isLead(currentUserId)) {
            if (currentUser.departmentId() == null) {
                throw new RuntimeException("Lead has no department");
            }

            department = departmentRepository.findById(currentUser.departmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
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