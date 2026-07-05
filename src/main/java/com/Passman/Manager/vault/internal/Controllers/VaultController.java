package com.Passman.Manager.vault.internal.Controllers;

import com.Passman.Manager.auth.internal.Security.MyUserDetails;
import com.Passman.Manager.management_roles.internal.Services.RolesManagementService;
import com.Passman.Manager.vault.DTO.*;
import com.Passman.Manager.vault.internal.Services.CategoryService;
import com.Passman.Manager.vault.internal.Services.EntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    private final CategoryService categoryService;
    private final RolesManagementService rolesManagementService;

    @Autowired
    public VaultController(EntryService entryService,
                           CategoryService categoryService,
                           RolesManagementService rolesManagementService) {
        this.entryService = entryService;
        this.categoryService = categoryService;
        this.rolesManagementService = rolesManagementService;
    }

    @GetMapping("/entries/{id}")
    public ResponseEntity<EntryDTO> showEntry(@AuthenticationPrincipal MyUserDetails userDetails,
                                              @PathVariable Long id) {
        if (!rolesManagementService.hasAccess(id, userDetails.getUser().getId())) {
            throw new AccessDeniedException("You do not have access to this entry.");
        }
        return ResponseEntity.ok(entryService.findById(id));
    }

    @PatchMapping("/entries/update/{id}")
    public ResponseEntity<EntryDTO> updatePassword(@AuthenticationPrincipal MyUserDetails userDetails,
                                                   @PathVariable Long id,
                                                   @RequestBody EntryDTO updatedEntryDTO) {
        if (!rolesManagementService.hasEditAccess(id, userDetails.getUser().getId())) {
            throw new AccessDeniedException("You do not have edit access to this entry.");
        }
        return ResponseEntity.ok(entryService.updateEntry(id, updatedEntryDTO, userDetails.getUser().getId()));
    }

    @DeleteMapping("/entries/{id}")
    public ResponseEntity<String> deleteEntry(@AuthenticationPrincipal MyUserDetails userDetails,
                                              @PathVariable Long id) {
        if (!rolesManagementService.hasEditAccess(id, userDetails.getUser().getId())) {
            throw new AccessDeniedException("You do not have edit access to this entry.");
        }
        entryService.delete(id);
        return ResponseEntity.ok("Entry deleted");
    }

    @PostMapping("/entries/create")
    public ResponseEntity<Void> createPassword(@AuthenticationPrincipal MyUserDetails userDetails,
                                               @RequestBody EntryGetDTO entryGetDTO) {
        entryService.save(entryGetDTO, userDetails.getUser().getId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/entries/all")
    public ResponseEntity<List<EntryDTO>> showAll(@AuthenticationPrincipal MyUserDetails userDetails) {
        return ResponseEntity.ok(entryService.findAllAccessibleAsDto(userDetails.getUser().getId()));
    }

    @GetMapping("/entries")
    public ResponseEntity<List<EntryDTO>> showEntriesByCategory(@AuthenticationPrincipal MyUserDetails userDetails,
                                                                @RequestParam String categoryName) {
        return ResponseEntity.ok(entryService.findAccessibleByCategory(categoryName, userDetails.getUser().getId()));
    }

    @GetMapping("/entries/count")
    public ResponseEntity<Long> showCountEntries(@AuthenticationPrincipal MyUserDetails userDetails) {
        return ResponseEntity.ok(entryService.findAccessibleCount(userDetails.getUser().getId()));
    }

    @GetMapping("/main")
    public ResponseEntity<Map<String, Long>> countEntriesByCategory(@AuthenticationPrincipal MyUserDetails userDetails) {
        return ResponseEntity.ok(entryService.findAccessibleCountEntriesByCategory(userDetails.getUser().getId()));
    }

    @GetMapping("/entries/meta")
    public ResponseEntity<CryptoDTO> sendMeta(@AuthenticationPrincipal MyUserDetails userDetails) {
        return ResponseEntity.ok(entryService.sendMeta(userDetails.getUser().getId()));
    }

    @PostMapping("/entries/meta")
    public ResponseEntity<String> getMeta(@AuthenticationPrincipal MyUserDetails userDetails,
                                          @RequestBody CryptoDTO cryptoDTO) {
        entryService.setMeta(userDetails.getUser().getId(), cryptoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("Meta added");
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> showCategories(@AuthenticationPrincipal MyUserDetails userDetails) {
        return ResponseEntity.ok(categoryService.findCategories(userDetails.getUser().getId()));
    }

    @PostMapping("/categories/add")
    public ResponseEntity<String> addCategory(@AuthenticationPrincipal MyUserDetails userDetails,
                                              @RequestBody CategoryDTO categoryDTO) {
        categoryService.addCategory(categoryDTO, userDetails.getUser().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body("Category added");
    }

    @PatchMapping("/categories/update")
    public ResponseEntity<String> updateCategory(@AuthenticationPrincipal MyUserDetails userDetails,
                                                 @RequestBody CategoryUpdateDTO categoryUpdateDTO) {
        categoryService.updateCategory(categoryUpdateDTO, userDetails.getUser().getId());
        return ResponseEntity.ok("Category updated");
    }

    @DeleteMapping("/categories/delete")
    public ResponseEntity<String> deleteCategory(@AuthenticationPrincipal MyUserDetails userDetails,
                                                 @RequestBody CategoryDTO categoryDTO) {
        categoryService.delete(categoryDTO, userDetails.getUser().getId());
        return ResponseEntity.ok("Category deleted");
    }
}