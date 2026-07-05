package com.Passman.Manager.management_roles.internal;


import com.Passman.Manager.management_roles.EntryKeyView;
import com.Passman.Manager.management_roles.ManagementRolesApi;
import com.Passman.Manager.management_roles.RoleView;
import com.Passman.Manager.management_roles.internal.Models.AccessRights;
import com.Passman.Manager.management_roles.internal.Models.EntryKey;
import com.Passman.Manager.management_roles.internal.Models.UserAccessRights;
import com.Passman.Manager.management_roles.internal.Models.UsersRoles;
import com.Passman.Manager.management_roles.internal.Repos.*;
import com.Passman.Manager.vault.EntryView;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ManagementRolesApiImplementation implements ManagementRolesApi {

    private final RoleRepository roleRepository;
    private final UsersRolesRepository usersRolesRepository;

    private final UserAccessRightsRepository userAccessRightsRepository;

    private final AccessRightsRepository accessRightsRepository;

    private final EntryKeyRepository entryKeyRepository;


    private final ModelMapper modelMapper;

    @Autowired
    public ManagementRolesApiImplementation(RoleRepository roleRepository, UsersRolesRepository usersRolesRepository, UserAccessRightsRepository userAccessRightsRepository, AccessRightsRepository accessRightsRepository, EntryKeyRepository entryKeyRepository, ModelMapper modelMapper) {
        this.roleRepository = roleRepository;
        this.usersRolesRepository = usersRolesRepository;
        this.userAccessRightsRepository = userAccessRightsRepository;
        this.accessRightsRepository = accessRightsRepository;
        this.entryKeyRepository = entryKeyRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public void saveEntry(Long entryId, Long userId, String encryptedDek, String dekIv, String dekEnvelopeType) {

        EntryKey entryKey = new EntryKey();
        entryKey.setEntryId(entryId);
        entryKey.setUserId(userId);
        entryKey.setEncryptedDek(encryptedDek);
        entryKey.setDekIv(dekIv);
        entryKey.setDekEnvelopeType(dekEnvelopeType);
        entryKeyRepository.save(entryKey);

        UserAccessRights rights = new UserAccessRights();
        rights.setEntryId(entryId);
        rights.setUserId(userId);
        rights.setCanView(true);
        rights.setCanEdit(true);
        userAccessRightsRepository.save(rights);
    }

    @Override
    public void addRoleToUserByName(Long userId, String roleName) {
        Optional<Long> roleId = roleRepository.findIdByName(roleName);
        roleId.ifPresent(aLong -> usersRolesRepository.save(new UsersRoles(userId, aLong)));
        throw new RuntimeException("No such role");
    }

    @Override
    public List<RoleView> findRolesByUserId(Long userId) {
        return usersRolesRepository.findRolesIdsByUserId(userId).stream()
                .map(roleRepository::findById)
                .filter(Optional::isPresent)
                .map(role -> modelMapper
                        .map(role, RoleView.class)).toList();
    }

    @Override
    public EntryKeyView findEntryKeyViewByEntryIdAndUserId(Long entryId, Long userId) {
        EntryKey entryKey = entryKeyRepository.findByEntryIdAndUserId(entryId, userId).orElseThrow(() -> new RuntimeException("EntryKey not found for entryId=" + entryId
                + ", userId=" + userId));

        return modelMapper.map(entryKey, EntryKeyView.class);

    }



    @Override
    public boolean[] resolveEntryPermissions(Long entryId, Long currentUserId, EntryView entryView) {
        boolean isOwner = entryView.userId() != null
                && entryView.userId().equals(currentUserId);

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

//    @Override
//    public RoleView findByName(String name) {
//        Optional<Role> roleOptional = roleRepository.findByName(name);
//        if (roleOptional.isPresent()){
//            return roleOptional.get();
//        }
//        else{
//
//        }
//    }

}
