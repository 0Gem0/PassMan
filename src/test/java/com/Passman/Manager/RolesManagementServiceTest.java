package com.Passman.Manager;

import com.Passman.Manager.auth.AuthApi;
import com.Passman.Manager.auth.UserView;
import com.Passman.Manager.management_roles.DTO.AccessRightsDTO;
import com.Passman.Manager.management_roles.DTO.ShareEntryDTO;
import com.Passman.Manager.management_roles.internal.Models.*;
import com.Passman.Manager.management_roles.internal.Repos.*;
import com.Passman.Manager.management_roles.internal.Services.RolesManagementService;
import com.Passman.Manager.shared.util.*;

import com.Passman.Manager.vault.EntryView;
import com.Passman.Manager.vault.VaultApi;

import com.Passman.Manager.management_roles.ManagementRolesApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.modelmapper.ModelMapper;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class RolesManagementServiceTest {

    @Mock
    private UserAccessRightsRepository userAccessRightsRepository;

    @Mock
    private EntryKeyRepository entryKeyRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private AccessRightsRepository accessRightsRepository;

    @Mock
    private ModelMapper mapper;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UsersRolesRepository usersRolesRepository;

    @Mock
    private VaultApi vaultApi;

    @Mock
    private AuthApi authApi;

    @Mock
    private ManagementRolesApi managementRolesApi;

    @InjectMocks
    private RolesManagementService service;

    private UserView user(
            Long id,
            Long departmentId
    ){

        return new UserView(
                id,
                "user",
                departmentId,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                null
        );
    }
    private Role role(
            Long id,
            Long departmentId
    ){
        Department dep = new Department();
        dep.setId(departmentId);

        Role role = new Role();
        role.setId(id);
        role.setDepartment(dep);
        role.setName("TEST_ROLE");

        return role;
    }

    @Test
    void adminShouldAssignAnyRole(){
        Long adminId = 1L;
        Long targetId = 2L;
        Long roleId = 10L;

        when(authApi.getUserViewById(adminId))
                .thenReturn(user(adminId,1L));

        when(authApi.getUserViewById(targetId))
                .thenReturn(user(targetId,5L));

        when(roleRepository.findById(roleId))
                .thenReturn(
                        Optional.of(role(roleId,5L))
                );

        when(usersRolesRepository
                .existsByUserIdAndRoleNameIgnoreCase(
                        adminId,
                        "ROLE_ADMIN"
                ))
                .thenReturn(true);

        when(usersRolesRepository
                .existsByUserIdAndRoleId(targetId,roleId))
                .thenReturn(false);

        service.assignRoleToUser(
                adminId,
                targetId,
                roleId
        );

        verify(usersRolesRepository)
                .save(any(UsersRoles.class));
    }

    @Test
    void shouldThrowWhenRoleDoesNotExist(){
        when(roleRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () ->
                        service.assignRoleToUser(
                                1L,
                                2L,
                                100L
                        )
        );

        verify(usersRolesRepository,never())
                .save(any());
    }

    @Test
    void ordinaryUserCannotAssignRole(){
        Long userId = 1L;

        when(authApi.getUserViewById(userId))
                .thenReturn(user(userId,1L));

        when(authApi.getUserViewById(2L))
                .thenReturn(user(2L,1L));

        when(roleRepository.findById(5L))
                .thenReturn(
                        Optional.of(role(5L,1L))
                );

        when(usersRolesRepository
                .existsByUserIdAndRoleNameIgnoreCase(
                        userId,
                        "ROLE_ADMIN"
                ))
                .thenReturn(false);

        when(usersRolesRepository
                .existsByUserIdAndRoleNameIgnoreCase(
                        userId,
                        "ROLE_LEAD"
                ))
                .thenReturn(false);

        assertThrows(
                ForbiddenOperationException.class,
                () ->
                        service.assignRoleToUser(
                                userId,
                                2L,
                                5L
                        )
        );

    }

    @Test
    void leadCannotAssignRoleFromAnotherDepartment(){
        Long leadId = 1L;

        when(authApi.getUserViewById(leadId))
                .thenReturn(user(leadId,1L));

        when(authApi.getUserViewById(2L))
                .thenReturn(user(2L,1L));

        when(roleRepository.findById(5L))
                .thenReturn(
                        Optional.of(role(5L,2L))
                );

        when(usersRolesRepository
                .existsByUserIdAndRoleNameIgnoreCase(
                        leadId,
                        "ROLE_ADMIN"
                ))
                .thenReturn(false);


        when(usersRolesRepository
                .existsByUserIdAndRoleNameIgnoreCase(
                        leadId,
                        "ROLE_LEAD"
                ))
                .thenReturn(true);

        assertThrows(
                ForbiddenOperationException.class,
                () ->
                        service.assignRoleToUser(
                                leadId,
                                2L,
                                5L
                        )
        );
    }

    @Test
    void leadCannotAssignRoleToUserFromAnotherDepartment(){
        Long leadId = 1L;

        when(authApi.getUserViewById(leadId))
                .thenReturn(user(leadId,1L));

        when(authApi.getUserViewById(2L))
                .thenReturn(user(2L,2L));

        when(roleRepository.findById(5L))
                .thenReturn(
                        Optional.of(role(5L,1L))
                );

        when(usersRolesRepository
                .existsByUserIdAndRoleNameIgnoreCase(
                        leadId,
                        "ROLE_ADMIN"
                ))
                .thenReturn(false);

        when(usersRolesRepository
                .existsByUserIdAndRoleNameIgnoreCase(
                        leadId,
                        "ROLE_LEAD"
                ))
                .thenReturn(true);

        assertThrows(
                ForbiddenOperationException.class,
                () ->
                        service.assignRoleToUser(
                                leadId,
                                2L,
                                5L
                        )
        );

    }

    @Test
    void assigningExistingRoleShouldNotCreateDuplicate(){
        Long adminId = 1L;

        when(authApi.getUserViewById(adminId))
                .thenReturn(user(adminId,1L));

        when(authApi.getUserViewById(2L))
                .thenReturn(user(2L,1L));

        when(roleRepository.findById(5L))
                .thenReturn(
                        Optional.of(role(5L,1L))
                );

        when(usersRolesRepository
                .existsByUserIdAndRoleNameIgnoreCase(
                        adminId,
                        "ROLE_ADMIN"
                ))
                .thenReturn(true);

        when(usersRolesRepository
                .existsByUserIdAndRoleId(2L,5L))
                .thenReturn(true);

        service.assignRoleToUser(
                adminId,
                2L,
                5L
        );

        verify(usersRolesRepository,never())
                .save(any());
    }

    @Test
    void adminShouldShareEntry(){
        Long adminId = 1L;
        Long targetUserId = 2L;
        Long entryId = 100L;

        UserView admin = user(adminId,1L);
        UserView target = user(targetUserId,5L);

        when(authApi.getUserViewById(adminId))
                .thenReturn(admin);

        when(authApi.getUserViewById(targetUserId))
                .thenReturn(target);

        when(usersRolesRepository
                .existsByUserIdAndRoleNameIgnoreCase(
                        adminId,
                        "ROLE_ADMIN"
                ))
                .thenReturn(true);

        when(vaultApi.entryExists(entryId))
                .thenReturn(true);

        ShareEntryDTO dto = new ShareEntryDTO();

        dto.setEntryId(entryId);
        dto.setTargetUserId(targetUserId);
        dto.setEncryptedDek("encrypted-dek");
        dto.setDekEnvelopeType("RSA");

        service.shareEntry(
                adminId,
                dto
        );

        verify(entryKeyRepository)
                .save(any(EntryKey.class));
    }

    @Test
    void leadShouldShareEntryInsideDepartment(){
        Long leadId = 1L;
        Long targetId = 2L;
        Long entryId = 10L;

        when(authApi.getUserViewById(leadId))
                .thenReturn(user(leadId,1L));

        when(authApi.getUserViewById(targetId))
                .thenReturn(user(targetId,1L));

        when(usersRolesRepository
                .existsByUserIdAndRoleNameIgnoreCase(
                        leadId,
                        "ROLE_ADMIN"
                ))
                .thenReturn(false);

        when(usersRolesRepository
                .existsByUserIdAndRoleNameIgnoreCase(
                        leadId,
                        "ROLE_LEAD"
                ))
                .thenReturn(true);

        when(vaultApi.entryExists(entryId))
                .thenReturn(true);

        when(managementRolesApi
                .hasAccess(entryId,leadId))
                .thenReturn(true);

        ShareEntryDTO dto = new ShareEntryDTO();

        dto.setEntryId(entryId);
        dto.setTargetUserId(targetId);
        dto.setEncryptedDek("dek");
        dto.setDekEnvelopeType("RSA");

        service.shareEntry(
                leadId,
                dto
        );

        verify(entryKeyRepository)
                .save(any(EntryKey.class));
    }

    @Test
    void shouldThrowWhenEncryptedDekMissing(){
        Long adminId = 1L;

        when(authApi.getUserViewById(adminId))
                .thenReturn(user(adminId,1L));



        when(vaultApi.entryExists(10L))
                .thenReturn(true);

        ShareEntryDTO dto = new ShareEntryDTO();

        dto.setEntryId(10L);
        dto.setTargetUserId(2L);
        dto.setDekEnvelopeType("RSA");

        assertThrows(
                NotValidException.class,
                () ->
                        service.shareEntry(
                                adminId,
                                dto
                        )
        );

        verify(entryKeyRepository,never())
                .save(any());
    }

    @Test
    void leadCannotShareEntryWithoutAccess(){
        Long leadId = 1L;
        Long targetId = 2L;
        Long entryId = 50L;

        when(authApi.getUserViewById(leadId))
                .thenReturn(user(leadId,1L));

        when(authApi.getUserViewById(targetId))
                .thenReturn(user(targetId,1L));

        when(usersRolesRepository
                .existsByUserIdAndRoleNameIgnoreCase(
                        leadId,
                        "ROLE_ADMIN"
                ))
                .thenReturn(false);

        when(usersRolesRepository
                .existsByUserIdAndRoleNameIgnoreCase(
                        leadId,
                        "ROLE_LEAD"
                ))
                .thenReturn(true);

        when(vaultApi.entryExists(entryId))
                .thenReturn(true);

        when(managementRolesApi
                .hasAccess(entryId,leadId))
                .thenReturn(false);

        ShareEntryDTO dto = new ShareEntryDTO();

        dto.setEntryId(entryId);
        dto.setTargetUserId(targetId);
        dto.setEncryptedDek("dek");
        dto.setDekEnvelopeType("RSA");

        assertThrows(
                ForbiddenOperationException.class,
                () ->
                        service.shareEntry(
                                leadId,
                                dto
                        )
        );

        verify(entryKeyRepository,never())
                .save(any());
    }
    @Test
    void revokeAccessFromUser_shouldDeleteAccessAndEntryKey(){
        Long adminId = 1L;
        Long targetUserId = 2L;
        Long entryId = 100L;

        EntryView entry = mock(EntryView.class);

        when(vaultApi.getEntryView(entryId))
                .thenReturn(entry);

        when(entry.id())
                .thenReturn(entryId);

        when(entry.userId())
                .thenReturn(5L); // не владелец

        when(authApi.getUserViewById(adminId))
                .thenReturn(user(adminId,1L));

        when(authApi.getUserViewById(targetUserId))
                .thenReturn(user(targetUserId,1L));

        when(usersRolesRepository
                .existsByUserIdAndRoleNameIgnoreCase(
                        adminId,
                        "ROLE_ADMIN"
                ))
                .thenReturn(true);

        when(managementRolesApi
                .hasAccess(entryId,targetUserId))
                .thenReturn(false);

        service.revokeAccessFromUser(
                adminId,
                targetUserId,
                entryId
        );

        verify(userAccessRightsRepository)
                .deleteByUserIdAndEntryId(
                        targetUserId,
                        entryId
                );

        verify(entryKeyRepository)
                .deleteByEntryIdAndUserId(
                        entryId,
                        targetUserId
                );
    }

    @Test
    void revokeAccessFromUser_shouldKeepEntryKeyWhenRoleStillProvidesAccess(){
        Long adminId = 1L;
        Long targetUserId = 2L;
        Long entryId = 100L;

        EntryView entry = mock(EntryView.class);

        when(entry.id())
                .thenReturn(entryId);

        when(entry.userId())
                .thenReturn(99L);

        when(vaultApi.getEntryView(entryId))
                .thenReturn(entry);

        when(authApi.getUserViewById(adminId))
                .thenReturn(user(adminId,1L));

        when(authApi.getUserViewById(targetUserId))
                .thenReturn(user(targetUserId,1L));

        when(usersRolesRepository
                .existsByUserIdAndRoleNameIgnoreCase(
                        adminId,
                        "ROLE_ADMIN"
                ))
                .thenReturn(true);

        // У пользователя остался доступ через роль
        when(managementRolesApi
                .hasAccess(
                        entryId,
                        targetUserId
                ))
                .thenReturn(true);

        service.revokeAccessFromUser(
                adminId,
                targetUserId,
                entryId
        );

        verify(userAccessRightsRepository)
                .deleteByUserIdAndEntryId(
                        targetUserId,
                        entryId
                );

        verify(entryKeyRepository,never())
                .deleteByEntryIdAndUserId(
                        anyLong(),
                        anyLong()
                );
    }

    @Test
    void revokeAccessFromUser_shouldNotDeleteOwnerKey(){
        Long adminId = 1L;
        Long ownerId = 2L;
        Long entryId = 100L;
        EntryView entry = mock(EntryView.class);


        when(entry.userId())
                .thenReturn(ownerId);

        when(vaultApi.getEntryView(entryId))
                .thenReturn(entry);

        when(authApi.getUserViewById(adminId))
                .thenReturn(user(adminId,1L));

        when(authApi.getUserViewById(ownerId))
                .thenReturn(user(ownerId,1L));

        when(usersRolesRepository
                .existsByUserIdAndRoleNameIgnoreCase(
                        adminId,
                        "ROLE_ADMIN"
                ))
                .thenReturn(true);

        service.revokeAccessFromUser(
                adminId,
                ownerId,
                entryId
        );

        verify(userAccessRightsRepository)
                .deleteByUserIdAndEntryId(
                        ownerId,
                        entryId
                );

        verify(entryKeyRepository,never())
                .deleteByEntryIdAndUserId(
                        anyLong(),
                        anyLong()
                );
    }

    @Test
    void grantAccessToRole_adminShouldGrantAccess(){
        Long adminId = 1L;
        Long entryId = 100L;
        Long roleId = 10L;

        when(authApi.getUserViewById(adminId))
                .thenReturn(user(adminId,1L));

        when(vaultApi.entryExists(entryId))
                .thenReturn(true);

        Role role = role(roleId,1L);
        when(roleRepository.findById(roleId))
                .thenReturn(
                        Optional.of(role)
                );

        when(usersRolesRepository
                .existsByUserIdAndRoleNameIgnoreCase(
                        adminId,
                        "ROLE_ADMIN"
                ))
                .thenReturn(true);

        AccessRightsDTO dto = new AccessRightsDTO();
        dto.setEntryId(entryId);
        dto.setRoleId(roleId);
        dto.setCanEdit(true);
        dto.setCanView(false);

        service.grantAccessToRole(
                adminId,
                dto
        );

        ArgumentCaptor<AccessRights> captor =
                ArgumentCaptor.forClass(
                        AccessRights.class
                );

        verify(accessRightsRepository)
                .save(captor.capture());

        AccessRights saved =
                captor.getValue();

        assertTrue(saved.isCanView());
        assertTrue(saved.isCanEdit());
    }

    @Test
    void leadCannotGrantAccessToAnotherDepartmentRole(){
        Long leadId = 1L;
        when(authApi.getUserViewById(leadId))
                .thenReturn(user(leadId,1L));

        when(vaultApi.entryExists(100L))
                .thenReturn(true);

        Role role = role(
                10L,
                2L // другой department
        );

        when(roleRepository.findById(10L))
                .thenReturn(
                        Optional.of(role)
                );

        when(usersRolesRepository
                .existsByUserIdAndRoleNameIgnoreCase(
                        leadId,
                        "ROLE_ADMIN"
                ))
                .thenReturn(false);

        when(usersRolesRepository
                .existsByUserIdAndRoleNameIgnoreCase(
                        leadId,
                        "ROLE_LEAD"
                ))
                .thenReturn(true);

        AccessRightsDTO dto = new AccessRightsDTO();
        dto.setEntryId(100L);
        dto.setRoleId(10L);
        dto.setCanEdit(true);

        assertThrows(
                ForbiddenOperationException.class,
                () ->
                        service.grantAccessToRole(
                                leadId,
                                dto
                        )
        );

        verify(accessRightsRepository,never())
                .save(any());

    }
}