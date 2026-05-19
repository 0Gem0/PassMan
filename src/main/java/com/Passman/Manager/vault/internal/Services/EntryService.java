package com.Passman.Manager.vault.internal.Services;

import com.Passman.Manager.auth.Models.User;
import com.Passman.Manager.shared.POJO.KdfParams;
import com.Passman.Manager.auth.internal.Repos.UserRepository;
import com.Passman.Manager.management_roles.internal.Models.EntryKey;
import com.Passman.Manager.management_roles.internal.Models.UserAccessRights;
import com.Passman.Manager.management_roles.internal.Repos.EntryKeyRepository;
import com.Passman.Manager.management_roles.internal.Repos.UserAccessRightsRepository;
import com.Passman.Manager.management_roles.internal.Services.RolesManagementService;
import com.Passman.Manager.vault.DTO.CryptoDTO;
import com.Passman.Manager.vault.DTO.EntryDTO;
import com.Passman.Manager.vault.DTO.EntryGetDTO;
import com.Passman.Manager.vault.Models.Category;
import com.Passman.Manager.vault.Models.Entry;
import com.Passman.Manager.vault.internal.Repos.CategoryRepository;
import com.Passman.Manager.vault.internal.Repos.EntryRepository;
import jakarta.persistence.Transient;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class EntryService {

    private final EntryRepository entryRepository;
    private final ModelMapper mapper;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EntryKeyRepository entryKeyRepository;
    private final RolesManagementService rolesManagementService;

    private final UserAccessRightsRepository userAccessRightsRepository;

    @Autowired
    public EntryService(EntryRepository entryRepository,
                        ModelMapper mapper,
                        CategoryRepository categoryRepository,
                        UserRepository userRepository, EntryKeyRepository entryKeyRepository, RolesManagementService rolesManagementService, UserAccessRightsRepository userAccessRightsRepository) {
        this.entryRepository = entryRepository;
        this.mapper = mapper;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.entryKeyRepository = entryKeyRepository;
        this.rolesManagementService = rolesManagementService;
        this.userAccessRightsRepository = userAccessRightsRepository;
    }

    public List<Entry> findAccessibleEntries(User user) {
        return entryRepository.findAccessibleEntries(user.getId());
    }

    public List<EntryDTO> findAllAccessibleAsDto(User user) {
        return entryRepository.findAccessibleEntries(user.getId())
                .stream()
                .map(entry -> toEntryDTO(entry, user))
                .collect(Collectors.toList());
    }


    public List<EntryDTO> findAccessibleByCategory(String categoryName, User user) {
        return entryRepository.findAccessibleEntriesByCategory(user.getId(), categoryName)
                .stream()
                .map(entry -> mapper.map(entry, EntryDTO.class))
                .collect(Collectors.toList());
    }

    public Long findAccessibleCount(User user) {
        return entryRepository.countAccessibleEntries(user.getId());
    }

    public Map<String, Long> findAccessibleCountEntriesByCategory(User user) {
        List<Entry> entries = entryRepository.findAccessibleEntries(user.getId());

        return entries.stream()
                .filter(entry -> entry.getCategory() != null)
                .collect(Collectors.groupingBy(
                        entry -> entry.getCategory().getName(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
    }

    public CryptoDTO sendMeta(long id) {
        User user = userRepository.findUserById(id);

        if (user == null || !user.isVaultInitialized()) {
            return new CryptoDTO(
                    false,
                    new KdfParams(),
                    "",
                    null,
                    null,
                    null
            );
        }
        return new CryptoDTO(
                true,
                user.getKdfParams(),
                user.getCryptoSalt(),
                user.getPublicKey(),
                user.getEncryptedPrivateKey(),
                user.getPrivateKeyIv()
        );
    }
//Сомнительно - маппить каждую entry
    public EntryDTO toEntryDTO(Entry entry, User currentUser) {
        EntryKey entryKey = entryKeyRepository
                .findByEntryIdAndUserId(entry.getId(), currentUser.getId())
                .orElseThrow(() -> new RuntimeException(
                        "EntryKey not found for entryId=" + entry.getId()
                                + ", userId=" + currentUser.getId()
                ));

        EntryDTO dto = new EntryDTO();
        mapper.map(entryKey, dto);
        mapper.map(entry, dto);
        boolean[] permissions = rolesManagementService.resolveEntryPermissions(entry, currentUser);
        dto.setCanView(permissions[0]);
        dto.setCanEdit(permissions[1]);

        return dto;
    }


    @Transactional
    public void setMeta(Long id, CryptoDTO cryptoDTO) {
        User user = userRepository.findUserById(id);
        if (user.isVaultInitialized()) {
            throw new RuntimeException("Vault already initialized");
        }
        user.setVaultInitialized(true);
        user.setKdfParams(cryptoDTO.getCryptoKdfParams());
        user.setCryptoSalt(cryptoDTO.getCryptoSalt());
        user.setPublicKey(cryptoDTO.getPublicKey());
        user.setEncryptedPrivateKey(cryptoDTO.getEncryptedPrivateKey());
        user.setPrivateKeyIv(cryptoDTO.getPrivateKeyIv());
    }

    @Transactional
    public EntryDTO updateEntry(Long id, EntryDTO updatedEntryDTO, Long currentUserId){
        Optional<Entry> optionalEntry = entryRepository.findEntryById(id);
        if (optionalEntry.isEmpty()){
            return null;
        }
        Entry entry = optionalEntry.get();
        if (entry.getUser() != null && entry.getUser().getId().equals(currentUserId)) {
            if (updatedEntryDTO.getCategoryName() != null) {
                Category category = categoryRepository.findCategoryByNameAndOwnerId(
                        updatedEntryDTO.getCategoryName(),
                        currentUserId
                );
                entry.setCategory(category);
            }
        }
        entry.setTitle(updatedEntryDTO.getTitle());
        entry.setEmail(updatedEntryDTO.getEmail());
        entry.setWebsite(updatedEntryDTO.getWebsite());
        EntryDTO entryDTO = new EntryDTO();
        mapper.map(entry, entryDTO);
        return entryDTO;
    }

    @Transactional
    public void delete(long id) {
        entryRepository.deleteById(id);
    }

    @Transactional
    public long save(EntryGetDTO entryDTO, long ownerId) {
        User user = userRepository.findUserById(ownerId);

        Category category = categoryRepository.findCategoryByNameAndOwnerId(
                entryDTO.getCategoryName(),
                ownerId
        );

        if (entryDTO.getEncryptedDek() == null || entryDTO.getEncryptedDek().isBlank()) {
            throw new RuntimeException("encryptedDek is required");
        }

        if (entryDTO.getDekIv() == null || entryDTO.getDekIv().isBlank()) {
            throw new RuntimeException("dekIv is required");
        }

        if (entryDTO.getDekEnvelopeType() == null || entryDTO.getDekEnvelopeType().isBlank()) {
            throw new RuntimeException("dekEnvelopeType is required");
        }

        Entry entry = new Entry();
        entry.setCategory(category);
        entry.setUser(user);

        Entry savedEntry = entryRepository.save(enrichEntry(entryDTO, entry));

        EntryKey entryKey = new EntryKey();
        entryKey.setEntry(savedEntry);
        entryKey.setUser(user);
        entryKey.setEncryptedDek(entryDTO.getEncryptedDek());
        entryKey.setDekIv(entryDTO.getDekIv());
        entryKey.setDekEnvelopeType(entryDTO.getDekEnvelopeType()); // KEK

        entryKeyRepository.save(entryKey);

        UserAccessRights userAccessRights = new UserAccessRights();
        userAccessRights.setEntry(savedEntry);
        userAccessRights.setUser(user);
        userAccessRights.setCanView(true);
        userAccessRights.setCanEdit(true);

        userAccessRightsRepository.save(userAccessRights);

        return savedEntry.getId();
    }

    public EntryDTO findById(long id) {
        Optional<Entry> optionalEntry = entryRepository.findEntryById(id);
        if (optionalEntry.isEmpty()) {
            return null;
        }

        EntryDTO entryDTO = new EntryDTO();
        mapper.map(optionalEntry.get(), entryDTO);
        return entryDTO;
    }

    @Transient
    public Entry enrichEntry(EntryGetDTO entryDTO, Entry entry) {
        mapper.map(entryDTO, entry);
        return entry;
    }
}