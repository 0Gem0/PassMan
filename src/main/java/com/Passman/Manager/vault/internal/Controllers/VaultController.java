package com.Passman.Manager.vault.internal.Controllers;

import com.Passman.Manager.shared.Security.UserPrincipal;
import com.Passman.Manager.management_roles.ManagementRolesApi;
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

    private final ManagementRolesApi managementRolesApi;

    @Autowired
    public VaultController(EntryService entryService,
                           CategoryService categoryService, ManagementRolesApi managementRolesApi) {
        this.entryService = entryService;
        this.categoryService = categoryService;
        this.managementRolesApi = managementRolesApi;
    }

    @GetMapping("/entries/{id}")
    public ResponseEntity<EntryDTO> showEntry(@AuthenticationPrincipal UserPrincipal userDetails,
                                              @PathVariable Long id) {
        if (!managementRolesApi.hasAccess(id, userDetails.getId())) {
            throw new AccessDeniedException("You do not have access to this entry.");
        }
        return ResponseEntity.ok(entryService.findById(id));
    }

    @PatchMapping("/entries/update/{id}")
    public ResponseEntity<EntryDTO> updatePassword(@AuthenticationPrincipal UserPrincipal userDetails,
                                                   @PathVariable Long id,
                                                   @RequestBody EntryDTO updatedEntryDTO) {
        if (!managementRolesApi.hasEditAccess(id, userDetails.getId())) {
            throw new AccessDeniedException("You do not have edit access to this entry.");
        }
        return ResponseEntity.ok(entryService.updateEntry(id, updatedEntryDTO, userDetails.getId()));
    }

    @DeleteMapping("/entries/{id}")
    public ResponseEntity<String> deleteEntry(@AuthenticationPrincipal UserPrincipal userDetails,
                                              @PathVariable Long id) {
        if (!managementRolesApi.hasEditAccess(id, userDetails.getId())) {
            throw new AccessDeniedException("You do not have edit access to this entry.");
        }
        entryService.delete(id);
        return ResponseEntity.ok("Entry deleted");
    }

    @PostMapping("/entries/create")
    public ResponseEntity<Void> createPassword(@AuthenticationPrincipal UserPrincipal userDetails,
                                               @RequestBody EntryGetDTO entryGetDTO) {
        entryService.save(entryGetDTO, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/entries/all")
    public ResponseEntity<List<EntryDTO>> showAll(@AuthenticationPrincipal UserPrincipal userDetails) {
        return ResponseEntity.ok(entryService.findAllAccessibleAsDto(userDetails.getId()));
    }

    @GetMapping("/entries")
    public ResponseEntity<List<EntryDTO>> showEntriesByCategory(@AuthenticationPrincipal UserPrincipal userDetails,
                                                                @RequestParam String categoryName) {
        return ResponseEntity.ok(entryService.findAccessibleByCategory(categoryName, userDetails.getId()));
    }

    @GetMapping("/entries/count")
    public ResponseEntity<Long> showCountEntries(@AuthenticationPrincipal UserPrincipal userDetails) {
        return ResponseEntity.ok(entryService.findAccessibleCount(userDetails.getId()));
    }

    @GetMapping("/main")
    public ResponseEntity<Map<String, Long>> countEntriesByCategory(@AuthenticationPrincipal UserPrincipal userDetails) {
        return ResponseEntity.ok(entryService.findAccessibleCountEntriesByCategory(userDetails.getId()));
    }

    @GetMapping("/entries/meta")
    public ResponseEntity<CryptoDTO> sendMeta(@AuthenticationPrincipal UserPrincipal userDetails) {
        return ResponseEntity.ok(entryService.sendMeta(userDetails.getId()));
    }

    @PostMapping("/entries/meta")
    public ResponseEntity<String> getMeta(@AuthenticationPrincipal UserPrincipal userDetails,
                                          @RequestBody CryptoDTO cryptoDTO) {
        entryService.setMeta(userDetails.getId(), cryptoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("Meta added");
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> showCategories(@AuthenticationPrincipal UserPrincipal userDetails) {
        return ResponseEntity.ok(categoryService.findCategories(userDetails.getId()));
    }

    @PostMapping("/categories/add")
    public ResponseEntity<String> addCategory(@AuthenticationPrincipal UserPrincipal userDetails,
                                              @RequestBody CategoryDTO categoryDTO) {
        categoryService.addCategory(categoryDTO, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body("Category added");
    }

    @PatchMapping("/categories/update")
    public ResponseEntity<String> updateCategory(@AuthenticationPrincipal UserPrincipal userDetails,
                                                 @RequestBody CategoryUpdateDTO categoryUpdateDTO) {
        categoryService.updateCategory(categoryUpdateDTO, userDetails.getId());
        return ResponseEntity.ok("Category updated");
    }

    @DeleteMapping("/categories/delete")
    public ResponseEntity<String> deleteCategory(@AuthenticationPrincipal UserPrincipal userDetails,
                                                 @RequestBody CategoryDTO categoryDTO) {
        categoryService.delete(categoryDTO, userDetails.getId());
        return ResponseEntity.ok("Category deleted");
    }
}