package com.Passman.Manager.management_roles;


import com.Passman.Manager.vault.EntryView;

import java.util.List;

public interface ManagementRolesApi {

    void saveEntry(Long entryId, Long userId, String encryptedDek, String dekIv, String dekEnvelopeType);
    void addRoleToUserByName(Long userId, String roleName);

    List<RoleView> findRolesByUserId(Long userId);

    EntryKeyView findEntryKeyViewByEntryIdAndUserId(Long userId, Long EntryId);


    boolean[] resolveEntryPermissions(Long entryId, Long currentUserId, EntryView entryView);

}
