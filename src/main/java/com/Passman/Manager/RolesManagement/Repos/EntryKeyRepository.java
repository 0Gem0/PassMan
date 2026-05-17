package com.Passman.Manager.RolesManagement.Repos;

import com.Passman.Manager.Auth.Models.User;
import com.Passman.Manager.RolesManagement.Models.EntryKey;
import com.Passman.Manager.Vault.Models.Entry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface EntryKeyRepository extends JpaRepository<EntryKey, Long> {

    boolean existsByEntryIdAndUserId(Long entryId, Long userId);

    Optional<EntryKey> findByEntryIdAndUserId(Long entryId, Long userId);

    List<EntryKey> findAllByUserId(Long userId);

    void deleteByEntryIdAndUserId(Long entryId, Long userId);

}