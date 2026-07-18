package com.Passman.Manager.management_roles.internal.Services;

import com.Passman.Manager.auth.AuthApi;
import com.Passman.Manager.auth.UserView;
import com.Passman.Manager.management_roles.DTO.*;
import com.Passman.Manager.management_roles.ManagementRolesApi;
import com.Passman.Manager.management_roles.internal.Models.*;
import com.Passman.Manager.management_roles.internal.Repos.*;
import com.Passman.Manager.shared.util.*;
import com.Passman.Manager.vault.EntryView;
import com.Passman.Manager.vault.VaultApi;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    private final ManagementRolesApi managementRolesApi;

    public RolesManagementService(UserAccessRightsRepository userAccessRightsRepository, EntryKeyRepository entryKeyRepository, DepartmentRepository departmentRepository,
                                  AccessRightsRepository accessRightsRepository,
                                  ModelMapper mapper,
                                  RoleRepository roleRepository, UsersRolesRepository usersRolesRepository, VaultApi vaultApi, AuthApi authApi, ManagementRolesApi managementRolesApi) {
        this.userAccessRightsRepository = userAccessRightsRepository;
        this.entryKeyRepository = entryKeyRepository;
        this.departmentRepository = departmentRepository;
        this.accessRightsRepository = accessRightsRepository;
        this.mapper = mapper;
        this.roleRepository = roleRepository;
        this.usersRolesRepository = usersRolesRepository;
        this.vaultApi = vaultApi;
        this.authApi = authApi;
        this.managementRolesApi = managementRolesApi;
    }


    public List<EntryRolesDTO> findAllAccessibleAsDtoShow(Long currentUserId) {
        List<EntryView> entries;

        if (isAdmin(currentUserId)) {
            entries = vaultApi.findAll();
        } else {
            entries = vaultApi.findAccessibleEntries(currentUserId);
        }

        return entries.stream()
                .filter(entry -> entryKeyRepository
                        .existsByEntryIdAndUserId(entry.id(), currentUserId))
                .map(entry -> toEntryDTO(entry, currentUserId))
                .collect(Collectors.toList());
    }

    public EntryRolesDTO toEntryDTO(EntryView entry, Long userId) {
        EntryKey entryKey = entryKeyRepository
                .findByEntryIdAndUserId(entry.id(), userId)
                .orElseThrow(() -> new NotFoundException(
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
    public UserPublicKeyDTO getUserPublicKey(Long currentUserId, Long targetUserId) {
        UserView currentUser = authApi.getUserViewById(currentUserId);
        UserView targetUser = authApi.getUserViewById(targetUserId);

        if (targetUser.publicKey() == null || targetUser.publicKey().isBlank()) {
            throw new InvalidStateException("Target user does not have a public key");
        }

        if (isAdmin(currentUserId)) {
            return new UserPublicKeyDTO(targetUser.publicKey());
        }

        if (!isLead(currentUserId)) {
            throw new ForbiddenOperationException("You are not allowed to view public keys.");
        }

        if (currentUser.departmentId() == null || targetUser.departmentId() == null) {
            throw new NotFoundException("Department is not defined.");
        }

        if (!currentUser.departmentId().equals(targetUser.departmentId())) {
            throw new ForbiddenOperationException("You cannot access users outside your department.");
        }

        return new UserPublicKeyDTO(targetUser.publicKey());
    }

    @Transactional
    public void shareEntry(Long currentUserId, ShareEntryDTO shareEntryDTO) {
        Long entryId = shareEntryDTO.getEntryId();
        Long targetUserId = shareEntryDTO.getTargetUserId();

        if (entryId == null) {
            throw new NotValidException("Entry id is required");
        }

        if (targetUserId == null) {
            throw new NotValidException("Target user id is required");
        }

        if (!vaultApi.entryExists(entryId)) {
            throw new NotFoundException("Entry not found");
        }

        UserView currentUser = authApi.getUserViewById(currentUserId);
        UserView targetUser = authApi.getUserViewById(targetUserId);

        if (shareEntryDTO.getEncryptedDek() == null || shareEntryDTO.getEncryptedDek().isBlank()) {
            throw new NotValidException("Encrypted DEK is required");
        }

        if (shareEntryDTO.getDekEnvelopeType() == null || shareEntryDTO.getDekEnvelopeType().isBlank()) {
            throw new NotValidException("DEK envelope type is required");
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
            throw new ForbiddenOperationException("You are not allowed to share entries.");
        }

        if (currentUser.departmentId() == null || targetUser.departmentId() == null) {
            throw new InvalidStateException("Department is not defined.");
        }

        if (!currentUser.departmentId().equals(targetUser.departmentId())) {
            throw new ForbiddenOperationException("You cannot share entries with users outside your department.");
        }

        if (!managementRolesApi.hasAccess(entryId, currentUserId)) {
            throw new ForbiddenOperationException("You cannot share an entry you do not have access to.");
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


    @Transactional(readOnly = true)
    public List<RoleDTO> getAssignableRoles(Long currentUserId) {
        List<Role> roles;

        UserView userView = authApi.getUserViewById(currentUserId);

        if (isAdmin(currentUserId)) {
            roles = roleRepository.findAll();
        } else if (isLead(currentUserId)) {
            roles = roleRepository.findByDepartmentId(userView.departmentId());
        } else {
            throw new ForbiddenOperationException("You are not allowed to assign roles.");
        }

        return roles.stream()
                .map(role -> mapper.map(role, RoleDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public void assignDepartment(Long currentUserId, AssignDepartmentDTO assignDepartmentDTO) {
        if (assignDepartmentDTO.getTargetUserId() == null) {
            throw new NotValidException("Target user id is required");
        }

        if (assignDepartmentDTO.getDepartmentId() == null) {
            throw new NotValidException("Department id is required");
        }

        UserView currentUser = authApi.getUserViewById(currentUserId);
        UserView targetUser = authApi.getUserViewById(assignDepartmentDTO.getTargetUserId());

        Department department = departmentRepository.findById(assignDepartmentDTO.getDepartmentId())
                .orElseThrow(() -> new NotFoundException("Department not found"));

        if (isAdmin(currentUserId)) {
            authApi.updateUserDepartment(targetUser.id(), department.getId());
            return;
        }

        if (!isLead(currentUserId)) {
            throw new ForbiddenOperationException("You are not allowed to assign departments");
        }

        if (currentUser.departmentId() == null) {
            throw new InvalidStateException("Current user has no department");
        }

        if (!currentUser.departmentId().equals(department.getId())) {
            throw new ForbiddenOperationException("Lead can assign only own department");
        }

        if (targetUser.departmentId() != null
                && !targetUser.departmentId().equals(currentUser.departmentId())) {
            throw new ForbiddenOperationException("Lead cannot move users from another department");
        }

        authApi.updateUserDepartment(targetUser.id(), department.getId());
    }

    @Transactional(readOnly = true)
    public List<UserPublicKeyDTO> getDepartmentWorkers(Long currentUserId) {
        UserView currentUser = authApi.getUserViewById(currentUserId);

        List<UserView> users;

        if (isAdmin(currentUserId)) {
            users = authApi.findAllUsers();
        } else if (isLead(currentUserId)) {
            if (currentUser.departmentId() == null) {
                throw new InvalidStateException("Current user has no department");
            }

            users = authApi.findUsersByDepartmentId(currentUser.departmentId());
        } else {
            throw new ForbiddenOperationException("You are not allowed to view department workers.");
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
            throw new InvalidStateException("Target user id is required");
        }

        if (roleId == null) {
            throw new InvalidStateException("Role id is required");
        }

        UserView currentUser = authApi.getUserViewById(currentUserId);
        UserView targetUser = authApi.getUserViewById(targetUserId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found"));

        if (isAdmin(currentUserId)) {
            addRoleIfMissing(targetUserId, roleId);
            return;
        }

        if (!isLead(currentUserId)) {
            throw new ForbiddenOperationException("You are not allowed to assign roles.");
        }

        if (currentUser.departmentId() == null) {
            throw new InvalidStateException("Current user has no department.");
        }

        if (role.getDepartment() == null) {
            throw new InvalidStateException("Role has no department.");
        }

        if (!currentUser.departmentId().equals(role.getDepartment().getId())) {
            throw new ForbiddenOperationException("You cannot assign a role outside your department.");
        }

        if (targetUser.departmentId() == null
                || !currentUser.departmentId().equals(targetUser.departmentId())) {
            throw new ForbiddenOperationException("You cannot assign a role to a user outside your department.");
        }

        addRoleIfMissing(targetUserId, roleId);
    }

    @Transactional
    public void removeRoleFromUser(Long currentUserId, Long targetUserId, Long roleId) {
        if (targetUserId == null) {
            throw new NotValidException("Target user id is required");
        }

        if (roleId == null) {
            throw new NotValidException("Role id is required");
        }

        UserView currentUser = authApi.getUserViewById(currentUserId);
        UserView targetUser = authApi.getUserViewById(targetUserId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found"));

        if (!usersRolesRepository.existsByUserIdAndRoleId(targetUserId, roleId)) {
            throw new ResourceConflictException("User does not have the selected role");
        }

        if (isAdmin(currentUserId)) {
            usersRolesRepository.deleteByUserIdAndRoleId(targetUserId, roleId);
            return;
        }

        if (!isLead(currentUserId)) {
            throw new ForbiddenOperationException("You are not allowed to remove roles.");
        }

        if (currentUser.departmentId() == null) {
            throw new InvalidStateException("Current user has no department.");
        }

        if (targetUser.departmentId() == null) {
            throw new InvalidStateException("Target user has no department.");
        }

        if (role.getDepartment() == null) {
            throw new InvalidStateException("Role has no department.");
        }

        if (!currentUser.departmentId().equals(role.getDepartment().getId())) {
            throw new ForbiddenOperationException("You cannot remove a role outside your department.");
        }

        if (!currentUser.departmentId().equals(targetUser.departmentId())) {
            throw new ForbiddenOperationException("You cannot remove a role from a user outside your department.");
        }

        usersRolesRepository.deleteByUserIdAndRoleId(targetUserId, roleId);
    }
    @Transactional
    public void grantAccessToRole(Long currentUserId, AccessRightsDTO accessRightsDTO) {
        if (accessRightsDTO.getEntryId() == null) {
            throw new NotValidException("Entry id is required");
        }

        if (accessRightsDTO.getRoleId() == null) {
            throw new NotValidException("Role id is required");
        }

        Long entryId = accessRightsDTO.getEntryId();
        Long roleId = accessRightsDTO.getRoleId();

        UserView currentUser = authApi.getUserViewById(currentUserId);

        if (!vaultApi.entryExists(entryId)) {
            throw new NotFoundException("Entry not found");
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found"));

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
            throw new ForbiddenOperationException("You are not allowed to grant access to roles.");
        }

        if (currentUser.departmentId() == null) {
            throw new InvalidStateException("Current user has no department.");
        }

        if (role.getDepartment() == null) {
            throw new InvalidStateException("Role has no department.");
        }

        if (!currentUser.departmentId().equals(role.getDepartment().getId())) {
            throw new ForbiddenOperationException("You cannot manage access for roles outside your department.");
        }

        if (!managementRolesApi.hasAccess(entryId, currentUserId)) {
            throw new ForbiddenOperationException("You cannot grant access to an entry you do not have access to.");
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
            throw new NotValidException("Target user id is required");
        }

        if (userAccessRightsDTO.getEntryId() == null) {
            throw new NotValidException("Entry id is required");
        }

        Long targetUserId = userAccessRightsDTO.getTargetUserId();
        Long entryId = userAccessRightsDTO.getEntryId();

        UserView currentUser = authApi.getUserViewById(currentUserId);
        UserView targetUser = authApi.getUserViewById(targetUserId);

        if (!vaultApi.entryExists(entryId)) {
            throw new NotFoundException("Entry not found");
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
            throw new ForbiddenOperationException("You are not allowed to grant personal access.");
        }

        if (currentUser.departmentId() == null) {
            throw new InvalidStateException("Current user has no department.");
        }

        if (targetUser.departmentId() == null) {
            throw new InvalidStateException("Target user has no department.");
        }

        if (!currentUser.departmentId().equals(targetUser.departmentId())) {
            throw new ForbiddenOperationException("You cannot manage users outside your department.");
        }

        if (!managementRolesApi.hasAccess(entryId, currentUserId)) {
            throw new ForbiddenOperationException("You cannot grant access to an entry you do not have access to.");
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
            throw new NotValidException("Target user id is required");
        }

        if (entryId == null) {
            throw new NotValidException("Entry id is required");
        }

        UserView currentUser = authApi.getUserViewById(currentUserId);
        UserView targetUser = authApi.getUserViewById(targetUserId);

        EntryView entry = vaultApi.getEntryView(entryId);

        if (isAdmin(currentUserId)) {
            userAccessRightsRepository.deleteByUserIdAndEntryId(targetUserId, entryId);
            deleteEntryKeyIfUserHasNoMoreAccess(entry, targetUserId);
            return;
        }

        if (!isLead(currentUserId)) {
            throw new ForbiddenOperationException("You are not allowed to revoke personal access.");
        }

        if (currentUser.departmentId() == null) {
            throw new InvalidStateException("Current user has no department.");
        }

        if (targetUser.departmentId() == null) {
            throw new InvalidStateException("Target user has no department.");
        }

        if (!currentUser.departmentId().equals(targetUser.departmentId())) {
            throw new ForbiddenOperationException("You cannot manage users outside your department.");
        }

        if (!managementRolesApi.hasAccess(entryId, currentUserId)) {
            throw new ForbiddenOperationException("You cannot revoke access to an entry you do not have access to.");
        }

        userAccessRightsRepository.deleteByUserIdAndEntryId(targetUserId, entryId);
        deleteEntryKeyIfUserHasNoMoreAccess(entry, targetUserId);
    }
    @Transactional
    public void revokeAccessFromRole(Long currentUserId, Long roleId, Long entryId) {

        EntryView entryView = vaultApi.getEntryView(entryId);


        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found"));

        AccessRights accessRights = accessRightsRepository
                .findByRoleIdAndEntryId(roleId, entryId)
                .orElseThrow(() -> new NotFoundException("Access rights not found"));

        checkCanManageRoleAccess(currentUserId, role, entryView);

        List<Long> usersIdsWithRole = usersRolesRepository.findUsersIdsByRoleId(roleId);

        accessRightsRepository.delete(accessRights);

        for (Long userId : usersIdsWithRole) {
            boolean shouldKeepKey = shouldUserKeepEntryKeyAfterRoleRevoke(userId, entryView);

            if (!shouldKeepKey) {
                entryKeyRepository.deleteByEntryIdAndUserId(entryId, userId);
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

        if (managementRolesApi.hasAccess(entry.id(), targetUserId)) {
            return;
        }

        entryKeyRepository.deleteByEntryIdAndUserId(entry.id(), targetUserId);
    }
    @Transactional(readOnly = true)
    public List<UserPublicKeyDTO> findUsersByRole(Long roleId, Long currentUserId) {
        if (roleId == null) {
            throw new NotValidException("Role id is required");
        }

        UserView currentUser = authApi.getUserViewById(currentUserId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("No role"));

        if (isAdmin(currentUserId)) {
            return usersRolesRepository.findUsersIdsByRoleId(roleId)
                    .stream()
                    .map(authApi::getUserViewById)
                    .map(this::toUserPublicKeyDTO)
                    .toList();
        }

        if (!isLead(currentUserId)) {
            throw new ForbiddenOperationException("No access");
        }

        if (currentUser.departmentId() == null) {
            throw new InvalidStateException("Current user has no department");
        }

        if (role.getDepartment() == null ||
                !currentUser.departmentId().equals(role.getDepartment().getId())) {
            throw new ForbiddenOperationException("Lead can view only users from own department role");
        }

        return usersRolesRepository.findUsersIdsByRoleId(roleId)
                .stream()
                .map(authApi::getUserViewById)
                .filter(user -> currentUser.departmentId().equals(user.departmentId()))
                .map(this::toUserPublicKeyDTO)
                .toList();
    }
    @Transactional(readOnly = true)
    public List<DepartmentDTO> getAssignableDepartments(Long currentUserId) {
        UserView currentUser = authApi.getUserViewById(currentUserId);

        if (isAdmin(currentUserId)) {
            return departmentRepository.findAll()
                    .stream()
                    .map(dep -> new DepartmentDTO(dep.getId(), dep.getName()))
                    .toList();
        }

        if (isLead(currentUserId)) {
            if (currentUser.departmentId() == null) {
                throw new InvalidStateException("Current user has no department");
            }

            Department department = departmentRepository.findById(currentUser.departmentId())
                    .orElseThrow(() -> new NotFoundException("Department not found"));

            return List.of(
                    new DepartmentDTO(
                            department.getId(),
                            department.getName()
                    )
            );
        }

        throw new ForbiddenOperationException("You are not allowed to assign departments");
    }
    private void checkCanManageRoleAccess(Long currentUserId, Role role, EntryView entry) {
        UserView currentUser = authApi.getUserViewById(currentUserId);

        if (isAdmin(currentUserId)) {
            return;
        }

        if (!isLead(currentUserId)) {
            throw new RuntimeException("You are not allowed to manage role access");
        }

        if (currentUser.departmentId() == null) {
            throw new InvalidStateException("Current user has no department");
        }

        if (role.getDepartment() == null) {
            throw new InvalidStateException("Role has no department");
        }

        if (!currentUser.departmentId().equals(role.getDepartment().getId())) {
            throw new ForbiddenOperationException("Lead can manage only roles from own department");
        }

        if (!entryKeyRepository.existsByEntryIdAndUserId(entry.id(), currentUserId)) {
            throw new InvalidStateException("Current user has no cryptographic access to this entry");
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
    public void createRole(Long currentUserId, CreateRoleDTO createRoleDTO) {
        if (createRoleDTO.getName() == null || createRoleDTO.getName().isBlank()) {
            throw new NotValidException("Role name is required");
        }

        String roleName = createRoleDTO.getName().trim();

        UserView currentUser = authApi.getUserViewById(currentUserId);

        Department department;

        if (isAdmin(currentUserId)) {
            if (createRoleDTO.getDepartmentId() == null) {
                throw new InvalidStateException("Department is required for admin role creation");
            }

            department = departmentRepository.findById(createRoleDTO.getDepartmentId())
                    .orElseThrow(() -> new NotFoundException("Department not found"));
        } else if (isLead(currentUserId)) {
            if (currentUser.departmentId() == null) {
                throw new InvalidStateException("Lead has no department");
            }

            department = departmentRepository.findById(currentUser.departmentId())
                    .orElseThrow(() -> new NotFoundException("Department not found"));
        } else {
            throw new ForbiddenOperationException("You are not allowed to create roles");
        }

        if (roleRepository.existsByNameAndDepartmentId(roleName, department.getId())) {
            throw new DuplicateResourceException("Role already exists in this department");
        }

        Role role = new Role();
        role.setName(roleName);
        role.setDepartment(department);

        roleRepository.save(role);
    }
}