package com.Passman.Manager.Vault.Controllers;


import com.Passman.Manager.Auth.Security.MyUserDetails;
import com.Passman.Manager.PassGen.Services.PasswordGeneratorService;
import com.Passman.Manager.Vault.DTO.CryptoDTO;
import com.Passman.Manager.Vault.DTO.EntryDTO;
import com.Passman.Manager.Vault.DTO.EntryGetDTO;
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

    @GetMapping("/entries/meta")
    public CryptoDTO sendMeta(@AuthenticationPrincipal MyUserDetails userDetails){
        return entryService.sendMeta(userDetails.getId());
    }

    @PostMapping("/entries/meta")
    public ResponseEntity<?> getMeta(@AuthenticationPrincipal MyUserDetails userDetails, @RequestBody CryptoDTO cryptoDTO){
        entryService.setMeta(userDetails.getId(), cryptoDTO);
        return ResponseEntity.ok("Meta added");
    }

    @PostMapping("/entries/addCategory")
    public ResponseEntity<?> addCategory(
            @AuthenticationPrincipal MyUserDetails userDetails, @RequestBody String categoryName) {
        Long id = userDetails.getId();
        entryService.addCategory(categoryName, id);
        return ResponseEntity.ok("Category Added");
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

    @DeleteMapping("/entries/{id}")
    public String deleteEntry(@PathVariable long id){
        entryService.delete(id);
        return "entry deleted";
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
    public long createPassword(@AuthenticationPrincipal MyUserDetails userDetails, @RequestBody EntryGetDTO entryGetDTO){
        return entryService.save(entryGetDTO, userDetails.getId());
    }

    @PatchMapping("/entries/update/{id}")
    public EntryDTO updatePassword(@AuthenticationPrincipal MyUserDetails userDetails,@PathVariable Long id, @RequestBody EntryDTO updatedEntryDTO){
        return entryService.updateEntry(id,updatedEntryDTO, userDetails.getId());
    }

}
