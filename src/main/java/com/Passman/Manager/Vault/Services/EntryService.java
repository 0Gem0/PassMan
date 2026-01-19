package com.Passman.Manager.Vault.Services;


import com.Passman.Manager.Auth.Models.User;
import com.Passman.Manager.Auth.POJO.KdfParams;
import com.Passman.Manager.Auth.Repos.UserRepository;
import com.Passman.Manager.Vault.DTO.CategoryCountDTO;
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

    @Autowired
    public EntryService(EntryRepository entryRepository, ModelMapper mapper, CategoryRepository categoryRepository, UserRepository userRepository) {
        this.entryRepository = entryRepository;
        this.mapper = mapper;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Map<String, Long> findCountEntriesByCategory(long userId){

        List<CategoryCountDTO> list = entryRepository.findCategoryCountsByUserId(userId);



         return list.stream().collect(Collectors.toMap(CategoryCountDTO::getCategoryName, CategoryCountDTO::getCountEntries));

    }



    public List<EntryDTO> findAllByCategory(String categoryName, long userId){
        Category category = categoryRepository.findCategoryByNameAndOwnerId(categoryName, userId);
        return entryRepository.findAllByCategoryIdAndUserId(category.getId(), userId).stream().map(entry -> mapper.map(entry,EntryDTO.class)).collect(Collectors.toList());
    }

    public List<EntryDTO> findAll(long userId){
        return entryRepository.findAllByUserId(userId).stream()
                .map(entry -> mapper.map(entry, EntryDTO.class))
                .collect(Collectors.toList());
    }
    public Long findCountEntries(long userId){
        return entryRepository.findCountAll(userId);
    }


    @Transactional
    public Long addCategory(String categoryName, Long userId){
        User user = userRepository.findUserById(userId);
        Category category = new Category();
        category.setName(categoryName);
        category.setSystem(false);
        category.setOwner(user);
        return categoryRepository.save(category).getId();
    }

    public CryptoDTO sendMeta(long id){
        Optional<byte[]> dek = userRepository.findUserDek(id);
        if (dek.isEmpty()){
            return new CryptoDTO(false, new KdfParams(), new byte[0]);
        }
        else{
            User user = userRepository.findUserById(id);
            return new CryptoDTO(true, user.getKdfParams(), user.getEncryptedDek());
        }
    }

    @Transactional
    public void setMeta(long id, CryptoDTO cryptoDTO){
        User user = userRepository.findUserById(id);
        user.setEncryptedDek(cryptoDTO.getEncryptedDek());
        user.setKdfParams(cryptoDTO.getKdfParams());
    }


    @Transactional
    public EntryDTO updateEntry(long id, EntryDTO updatedEntryDTO, long userId){
        Optional<Entry> optionalEntry = entryRepository.findEntryById(id);
        if (optionalEntry.isEmpty()){
            return null;
        }
        Entry entry = optionalEntry.get();
        Category category = categoryRepository.findCategoryByNameAndOwnerId(updatedEntryDTO.getCategoryName(), userId);
        entry.setCategory(category);
        entry.setTitle(updatedEntryDTO.getTitle());
        entry.setEmail(updatedEntryDTO.getEmail());
        entry.setWebsite(updatedEntryDTO.getWebsite());
        EntryDTO entryDTO = new EntryDTO();
        mapper.map(entry,entryDTO);
        return entryDTO;
    }

    @Transactional
    public void delete(long id){
        entryRepository.deleteById(id);
    }

    @Transactional
    public long save(EntryGetDTO entryDTO, long ownerId){
        Entry entry = new Entry();
        User user = userRepository.findUserById(ownerId);
        Category category = categoryRepository.findCategoryByNameAndOwnerId(entryDTO.getCategoryName(),ownerId);
        entry.setCategory(category);
        entry.setUser(user);
        Entry entry1 = entryRepository.save(enrichEntry(entryDTO, entry));
        return entry1.getId();
    }

    public EntryDTO findById(long id){
        Optional<Entry> optionalEntry = entryRepository.findEntryById(id);
        if (optionalEntry.isEmpty()){
            return null;
        }
        EntryDTO entryDTO = new EntryDTO();
        mapper.map(optionalEntry.get(), entryDTO);
        return entryDTO;
    }


    @Transient
    public Entry enrichEntry(EntryGetDTO entryDTO, Entry entry){
        mapper.map(entryDTO, entry);
        return entry;
    }

}
