package com.Passman.Manager.vault.internal.Services;

import com.Passman.Manager.vault.DTO.CategoryDTO;
import com.Passman.Manager.vault.DTO.CategoryUpdateDTO;
import com.Passman.Manager.vault.internal.Models.Category;
import com.Passman.Manager.vault.internal.Repos.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;


    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<String> findCategories(long ownerId){
        return categoryRepository.findCategoriesByOwnerIdOrSystem(ownerId);
    }

    @Transactional
    public Long addCategory(CategoryDTO categoryDTO, Long userId){
        Category category = new Category();
        category.setName(categoryDTO.getUpdatedName());
        category.setSystem(false);
        category.setOwnerId(userId);
        return categoryRepository.save(category).getId();
    }

    @Transactional
    public void updateCategory(CategoryUpdateDTO categoryUpdateDTO, Long userId){
        Optional<Category> categoryOptional = categoryRepository.findCategoryByNameAndOwnerId(categoryUpdateDTO.getName(), userId);
        if (categoryOptional.isPresent()){
            categoryOptional.get().setName(categoryUpdateDTO.getUpdatedName());
        }
        else throw new RuntimeException("No category found");
    }

    @Transactional
    public void delete(CategoryDTO categoryDTO, Long userId){
        categoryRepository.deleteByNameAndOwnerId(categoryDTO.getUpdatedName(),userId);
    }

}
