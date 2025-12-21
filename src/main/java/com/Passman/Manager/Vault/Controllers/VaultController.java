package com.Passman.Manager.Vault.Controllers;


import com.Passman.Manager.Auth.Security.MyUserDetails;
import com.Passman.Manager.PassGen.Services.PasswordGeneratorService;
import com.Passman.Manager.Vault.DTO.EntryDTO;
import com.Passman.Manager.Vault.DTO.PasswordGenerationDTO;
import com.Passman.Manager.Vault.Models.Entry;
import com.Passman.Manager.Vault.Services.CategoryService;
import com.Passman.Manager.Vault.Services.EntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/vault")
public class VaultController {

    private final EntryService entryService;
    private final PasswordGeneratorService passwordGeneratorService;
    private final CategoryService categoryService;

    @Autowired
    public VaultController(EntryService entryService, PasswordGeneratorService passwordGeneratorService, CategoryService categoryService) {
        this.entryService = entryService;
        this.passwordGeneratorService = passwordGeneratorService;
        this.categoryService = categoryService;
    }

    @GetMapping("/main")
    public Map<String, Long> countEntriesByCategory(
            @AuthenticationPrincipal MyUserDetails userDetails) {
        Long id = userDetails.getId();
        System.out.println(id);
        return entryService.findCountEntriesByCategory(id);
    }

    @GetMapping("/entries")
    public List<EntryDTO> showEntriesByCategory(
            @AuthenticationPrincipal MyUserDetails userDetails, @RequestParam String categoryName) {
        Long id = userDetails.getId();
        return entryService.findAllByCategory(categoryName, id);
    }

    @GetMapping("/entries/count")
    public Long showCountEntries(
            @AuthenticationPrincipal MyUserDetails userDetails) {
        Long id = userDetails.getId();
        return entryService.findCountEntries(id);
    }

    @GetMapping("/entries/all")
    public List<EntryDTO> showAll(
            @AuthenticationPrincipal MyUserDetails userDetails) {
        Long id = userDetails.getId();
        return entryService.findAll(id);
    }

    @GetMapping("/entries/{id}")
    public EntryDTO showEntry(
            @AuthenticationPrincipal MyUserDetails userDetails, @PathVariable Long id){
        return entryService.findById(id);
    }

    @GetMapping("/categories")
    public List<String> showCategories(
            @AuthenticationPrincipal MyUserDetails userDetails) {
        Long id = userDetails.getId();
        return categoryService.findCategories(id);
    }

    @PostMapping("/entries/generate")
    public String generatePassword(@RequestBody PasswordGenerationDTO passwordGenerationDTO){
        return passwordGeneratorService.generatePassword(passwordGenerationDTO);
    }

    @PostMapping("/entries/create")
    public long createPassword(@AuthenticationPrincipal MyUserDetails userDetails, @RequestBody EntryDTO entryDTO){
        return entryService.save(entryDTO, userDetails.getId());
    }

    @PatchMapping("/entries/update/{id}")
    public EntryDTO updatePassword(@AuthenticationPrincipal MyUserDetails userDetails,@PathVariable Long id, @RequestBody EntryDTO updatedEntryDTO){
        return entryService.updateEntry(id,updatedEntryDTO, userDetails.getId());
    }

}
