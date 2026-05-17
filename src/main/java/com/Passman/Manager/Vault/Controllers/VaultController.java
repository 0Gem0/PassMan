package com.Passman.Manager.Vault.Controllers;

import com.Passman.Manager.Auth.Models.User;
import com.Passman.Manager.Auth.Security.MyUserDetails;
import com.Passman.Manager.PassGen.Services.PasswordGeneratorService;
import com.Passman.Manager.RolesManagement.Services.RolesManagementService;
import com.Passman.Manager.Vault.DTO.*;
import com.Passman.Manager.Vault.Services.CategoryService;
import com.Passman.Manager.Vault.Services.EntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
    private final RolesManagementService rolesManagementService;

    @Autowired
    public VaultController(EntryService entryService,
                           PasswordGeneratorService passwordGeneratorService,
                           CategoryService categoryService,
                           RolesManagementService rolesManagementService) {
        this.entryService = entryService;
        this.passwordGeneratorService = passwordGeneratorService;
        this.categoryService = categoryService;
        this.rolesManagementService = rolesManagementService;
    }

//    @GetMapping("/all")
//    public List<Entry> getAccessibleEntries(@AuthenticationPrincipal MyUserDetails currentUser) {
//        return entryService.findAccessibleEntries(currentUser.getUser());
//    }

    @GetMapping("/entries/{id}")
    public EntryDTO showEntry(@AuthenticationPrincipal MyUserDetails userDetails,
                              @PathVariable Long id) {
        User currentUser = userDetails.getUser();

        if (!rolesManagementService.hasAccess(id, currentUser)) {
            throw new AccessDeniedException("You do not have access to this entry.");
        }

        return entryService.findById(id);
    }

    @PatchMapping("/entries/update/{id}")
    public EntryDTO updatePassword(@AuthenticationPrincipal MyUserDetails userDetails,
                                   @PathVariable Long id,
                                   @RequestBody EntryDTO updatedEntryDTO) {
        User currentUser = userDetails.getUser();

        if (!rolesManagementService.hasEditAccess(id, currentUser)) {
            throw new AccessDeniedException("You do not have edit access to this entry.");
        }

        return entryService.updateEntry(id, updatedEntryDTO, userDetails.getId());
    }

    @DeleteMapping("/entries/{id}")
    public ResponseEntity<?> deleteEntry(@AuthenticationPrincipal MyUserDetails userDetails,
                                         @PathVariable Long id) {
        User currentUser = userDetails.getUser();

        if (!rolesManagementService.hasEditAccess(id, currentUser)) {
            throw new AccessDeniedException("You do not have edit access to this entry.");
        }

        entryService.delete(id);
        return ResponseEntity.ok("Entry deleted");
    }

    @PostMapping("/entries/create")
    public long createPassword(@AuthenticationPrincipal MyUserDetails userDetails,
                               @RequestBody EntryGetDTO entryGetDTO) {
        return entryService.save(entryGetDTO, userDetails.getId());
    }

    @PostMapping("/entries/generate")
    public String generatePassword(@RequestBody PasswordGenerationDTO passwordGenerationDTO) {
        return passwordGeneratorService.generatePassword(passwordGenerationDTO);
    }

    @GetMapping("/entries/all")
    public List<EntryDTO> showAll(@AuthenticationPrincipal MyUserDetails userDetails) {
        return entryService.findAllAccessibleAsDto(userDetails.getUser());
    }

    @GetMapping("/entries")
    public List<EntryDTO> showEntriesByCategory(@AuthenticationPrincipal MyUserDetails userDetails,
                                                @RequestParam String categoryName) {
        return entryService.findAccessibleByCategory(categoryName, userDetails.getUser());
    }

    @GetMapping("/entries/count")
    public Long showCountEntries(@AuthenticationPrincipal MyUserDetails userDetails) {
        return entryService.findAccessibleCount(userDetails.getUser());
    }

    @GetMapping("/main")
    public Map<String, Long> countEntriesByCategory(@AuthenticationPrincipal MyUserDetails userDetails) {
        return entryService.findAccessibleCountEntriesByCategory(userDetails.getUser());
    }

    @GetMapping("/entries/meta")
    public CryptoDTO sendMeta(@AuthenticationPrincipal MyUserDetails userDetails) {
        return entryService.sendMeta(userDetails.getId());
    }

    @PostMapping("/entries/meta")
    public ResponseEntity<?> getMeta(@AuthenticationPrincipal MyUserDetails userDetails,
                                     @RequestBody CryptoDTO cryptoDTO) {
        entryService.setMeta(userDetails.getId(), cryptoDTO);
        return ResponseEntity.ok("Meta added");
    }

    @GetMapping("/categories")
    public List<String> showCategories(@AuthenticationPrincipal MyUserDetails userDetails) {
        return categoryService.findCategories(userDetails.getUser().getId());
    }

    @PostMapping("/categories/add")
    public ResponseEntity<?> addCategory(@AuthenticationPrincipal MyUserDetails userDetails,
                                         @RequestBody CategoryDTO categoryDTO) {
        categoryService.addCategory(categoryDTO, userDetails.getId());
        return ResponseEntity.ok("Category added");
    }

    @PatchMapping("/categories/update")
    public ResponseEntity<?> updateCategory(@AuthenticationPrincipal MyUserDetails userDetails,
                                            @RequestBody CategoryUpdateDTO categoryUpdateDTO) {
        categoryService.updateCategory(categoryUpdateDTO, userDetails.getId());
        return ResponseEntity.ok("Category updated");
    }

    @DeleteMapping("/categories/delete")
    public ResponseEntity<?> deleteCategory(@AuthenticationPrincipal MyUserDetails userDetails,
                                            @RequestBody CategoryDTO categoryDTO) {
        categoryService.delete(categoryDTO, userDetails.getId());
        return ResponseEntity.ok("Category deleted");
    }
}