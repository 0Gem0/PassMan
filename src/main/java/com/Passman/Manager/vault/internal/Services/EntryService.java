package com.Passman.Manager.vault.internal.Services;

import com.Passman.Manager.auth.AuthApi;
import com.Passman.Manager.auth.UserView;
import com.Passman.Manager.management_roles.EntryKeyView;
import com.Passman.Manager.management_roles.ManagementRolesApi;
import com.Passman.Manager.shared.POJO.KdfParams;
import com.Passman.Manager.shared.util.InvalidStateException;
import com.Passman.Manager.shared.util.NotFoundException;
import com.Passman.Manager.vault.DTO.CryptoDTO;
import com.Passman.Manager.vault.DTO.EntryDTO;
import com.Passman.Manager.vault.DTO.EntryGetDTO;
import com.Passman.Manager.vault.EntryView;
import com.Passman.Manager.vault.internal.Models.Category;
import com.Passman.Manager.vault.internal.Models.Entry;
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

    private final ManagementRolesApi managementRolesApi;

    private final AuthApi authApi;


    @Autowired
    public EntryService(EntryRepository entryRepository, ModelMapper mapper, CategoryRepository categoryRepository, ManagementRolesApi managementRolesApi, AuthApi authApi) {
        this.entryRepository = entryRepository;
        this.mapper = mapper;
        this.categoryRepository = categoryRepository;
        this.managementRolesApi = managementRolesApi;
        this.authApi = authApi;
    }

    public List<EntryDTO> findAllAccessibleAsDto(Long userId) {
        return entryRepository.findAccessibleEntries(userId)
                .stream()
                .map(entry -> toEntryDTO(entry, userId))
                .collect(Collectors.toList());
    }


    public List<EntryDTO> findAccessibleByCategory(String categoryName, Long userId) {
        return entryRepository.findAccessibleEntriesByCategory(userId, categoryName)
                .stream()
                .map(entry -> mapper.map(entry, EntryDTO.class))
                .collect(Collectors.toList());
    }

    public Long findAccessibleCount(Long userId) {
        return entryRepository.countAccessibleEntries(userId);
    }

    public Map<String, Long> findAccessibleCountEntriesByCategory(Long userId) {
        List<Entry> entries = entryRepository.findAccessibleEntries(userId);

        return entries.stream()
                .filter(entry -> entry.getCategory() != null)
                .collect(Collectors.groupingBy(
                        entry -> entry.getCategory().getName(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
    }

    public CryptoDTO sendMeta(Long userId) {
        UserView userView = authApi.getUserViewById(userId);

        if (userView == null || !userView.vaultInitialized()) {
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
                userView.kdfParams(),
                userView.cryptoSalt(),
                userView.publicKey(),
                userView.encryptedPrivateKey(),
                userView.privateKeyIv()
        );
    }
//Сомнительно - маппить каждую entry
    public EntryDTO toEntryDTO(Entry entry, Long currentUserId) {
        EntryKeyView entryKeyView = managementRolesApi.findEntryKeyViewByEntryIdAndUserId(currentUserId,entry.getId());

        EntryDTO dto = new EntryDTO();
        mapper.map(entryKeyView, dto);
        mapper.map(entry, dto);
        boolean[] permissions = managementRolesApi.resolveEntryPermissions(entry.getId(), currentUserId, mapper.map(entry, EntryView.class));
        dto.setCanView(permissions[0]);
        dto.setCanEdit(permissions[1]);

        return dto;
    }



    @Transactional
    public void setMeta(Long userId, CryptoDTO cryptoDTO) {
        authApi.initializeVault(userId, cryptoDTO);
    }

    @Transactional
    public EntryDTO updateEntry(Long id, EntryDTO updatedEntryDTO, Long currentUserId){
        Optional<Entry> optionalEntry = entryRepository.findEntryById(id);
        if (optionalEntry.isEmpty()){
            return null;
        }
        Entry entry = optionalEntry.get();
        if (entry.getUserId() != null && entry.getUserId().equals(currentUserId)) {
            if (updatedEntryDTO.getCategoryName() != null) {
                Optional<Category> category = categoryRepository.findCategoryByNameAndOwnerId(
                        updatedEntryDTO.getCategoryName(),
                        currentUserId
                );
                if (category.isPresent()){
                    entry.setCategory(category.get());
                }
                else throw new NotFoundException("No category" +  updatedEntryDTO.getCategoryName() + "found");
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
    public void delete(Long id) {
        entryRepository.deleteById(id);
    }


    @Transactional
    public void save(EntryGetDTO entryDTO, Long ownerId) {

        Category category = categoryRepository.findCategoryByNameAndOwnerId(
                entryDTO.getCategoryName(),
                ownerId
        ).orElseThrow(() -> new NotFoundException("No category" + entryDTO.getCategoryName() + "found"));

        if (entryDTO.getEncryptedDek() == null || entryDTO.getEncryptedDek().isBlank()) {
            throw new InvalidStateException("encryptedDek is required");
        }

        if (entryDTO.getDekIv() == null || entryDTO.getDekIv().isBlank()) {
            throw new InvalidStateException("dekIv is required");
        }

        if (entryDTO.getDekEnvelopeType() == null || entryDTO.getDekEnvelopeType().isBlank()) {
            throw new InvalidStateException("dekEnvelopeType is required");
        }

        Entry entry = new Entry();
        entry.setCategory(category);
        entry.setUserId(ownerId);

        Entry savedEntry = entryRepository.save(enrichEntry(entryDTO, entry));

        managementRolesApi.saveEntry(savedEntry.getId(), ownerId, entryDTO.getEncryptedDek(), entryDTO.getDekIv(), entryDTO.getDekEnvelopeType());
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