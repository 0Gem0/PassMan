package com.Passman.Manager.Vault.Services;

import com.Passman.Manager.Auth.Models.User;
import com.Passman.Manager.Auth.POJO.KdfParams;
import com.Passman.Manager.Auth.Repos.UserRepository;
import com.Passman.Manager.RolesManagement.DTO.EntryShowDTO;
import com.Passman.Manager.RolesManagement.Models.UserAccessRights;
import com.Passman.Manager.RolesManagement.Repos.UserAccessRightsRepository;
import com.Passman.Manager.Vault.DTO.CryptoDTO;
import com.Passman.Manager.Vault.DTO.EntryDTO;
import com.Passman.Manager.Vault.DTO.EntryGetDTO;
import com.Passman.Manager.Vault.Models.Category;
import com.Passman.Manager.Vault.Models.Entry;
import com.Passman.Manager.Vault.Repos.CategoryRepository;
import com.Passman.Manager.Vault.Repos.EntryRepository;
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

    private final UserAccessRightsRepository userAccessRightsRepository;

    @Autowired
    public EntryService(EntryRepository entryRepository,
                        ModelMapper mapper,
                        CategoryRepository categoryRepository,
                        UserRepository userRepository, UserAccessRightsRepository userAccessRightsRepository) {
        this.entryRepository = entryRepository;
        this.mapper = mapper;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.userAccessRightsRepository = userAccessRightsRepository;
    }

    public List<Entry> findAccessibleEntries(User user) {
        return entryRepository.findAccessibleEntries(user.getId());
    }

    public List<EntryDTO> findAllAccessibleAsDto(User user) {
        return entryRepository.findAccessibleEntries(user.getId())
                .stream()
                .map(entry -> mapper.map(entry, EntryDTO.class))
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
        Optional<byte[]> dek = userRepository.findUserDek(id);
        if (dek.isEmpty()) {
            return new CryptoDTO(false, new KdfParams(), new byte[0], new byte[0], new byte[0]);
        } else {
            User user = userRepository.findUserById(id);
            return new CryptoDTO(
                    true,
                    user.getKdfParams(),
                    user.getEncryptedDek(),
                    user.getEncryptedDekIv(),
                    user.getCryptoSalt()
            );
        }
    }

    @Transactional
    public void setMeta(long id, CryptoDTO cryptoDTO) {
        User user = userRepository.findUserById(id);
        user.setKdfParams(cryptoDTO.getCryptoKdfParams());
        user.setEncryptedDek(cryptoDTO.getEncryptedDEK());
        user.setEncryptedDekIv(cryptoDTO.getEncryptedDEK_iv());
        user.setCryptoSalt(cryptoDTO.getCryptoSalt());
    }

    @Transactional
    public EntryDTO updateEntry(long id, EntryDTO updatedEntryDTO, long currentUserId){
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
        UserAccessRights userAccessRights = new UserAccessRights();
        Entry entry = new Entry();
        User user = userRepository.findUserById(ownerId);
        Category category = categoryRepository.findCategoryByNameAndOwnerId(
                entryDTO.getCategoryName(),
                ownerId
        );

        entry.setCategory(category);
        entry.setUser(user);

        Entry savedEntry = entryRepository.save(enrichEntry(entryDTO, entry));
        userAccessRights.setEntry(savedEntry);
        userAccessRights.setUser(user);
        userAccessRights.setCanEdit(true);
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