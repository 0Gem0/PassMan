package com.Passman.Manager.vault.internal;

import com.Passman.Manager.auth.internal.Models.User;
import com.Passman.Manager.vault.DTO.EntryDTO;
import com.Passman.Manager.vault.EntryView;
import com.Passman.Manager.vault.VaultApi;
import com.Passman.Manager.vault.internal.Models.Entry;
import com.Passman.Manager.vault.internal.Repos.EntryRepository;
import jdk.jfr.StackTrace;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
public class VaultApiImplementation implements VaultApi {

    private final EntryRepository entryRepository;

    private final ModelMapper modelMapper;


    @Autowired
    public VaultApiImplementation(EntryRepository entryRepository, ModelMapper modelMapper) {
        this.entryRepository = entryRepository;
        this.modelMapper = modelMapper;
    }

//    public List<EntryDTO> findAllAccessibleAsDtoShow(Long currentUserId) {
//        List<Entry> entries;
//
//        if (isAdmin(currentUser)) {
//            entries = entryRepository.findAll();
//        } else {
//            entries = entryRepository.findAccessibleEntries(currentUser.getId());
//        }
//
//        return entries.stream()
//                .filter(entry -> entryKeyRepository
//                        .existsByEntryIdAndUserId(entry.getId(), currentUser.getId()))
//                .map(entry -> toEntryDTO(entry, currentUser))
//                .collect(Collectors.toList());
//    }

    @Override
    public List<EntryView> findAll() {
        List<Entry> entries = entryRepository.findAll();
        return entries.stream().map(entry -> modelMapper.map(entry, EntryView.class)).collect(Collectors.toList());
    }


    @Override
    public List<EntryView> findAccessibleEntries(Long userId) {
        List<Entry> entries = entryRepository.findAccessibleEntries(userId);
        return entries.stream().map(entry -> modelMapper.map(entry, EntryView.class)).collect(Collectors.toList());
    }
}
