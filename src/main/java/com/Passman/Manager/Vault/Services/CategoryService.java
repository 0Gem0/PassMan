package com.Passman.Manager.Vault.Services;


import com.Passman.Manager.Auth.Models.User;
import com.Passman.Manager.Auth.Repos.UserRepository;
import com.Passman.Manager.Vault.DTO.CategoryDTO;
import com.Passman.Manager.Vault.DTO.CategoryUpdateDTO;
import com.Passman.Manager.Vault.Models.Category;
import com.Passman.Manager.Vault.Repos.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;


    @Autowired
    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public List<String> findCategories(long ownerId){
        return categoryRepository.findCategoriesByOwnerIdOrSystem(ownerId);
    }

    @Transactional
    public Long addCategory(CategoryDTO categoryDTO, Long userId){
        User user = userRepository.findUserById(userId);
        Category category = new Category();
        category.setName(categoryDTO.getUpdatedName());
        category.setSystem(false);
        category.setOwner(user);
        return categoryRepository.save(category).getId();
    }

    @Transactional
    public void updateCategory(CategoryUpdateDTO categoryUpdateDTO, Long userId){
        Category category = categoryRepository.findCategoryByNameAndOwnerId(categoryUpdateDTO.getName(), userId);
        category.setName(categoryUpdateDTO.getUpdatedName());
    }

    @Transactional
    public void delete(CategoryDTO categoryDTO, Long userId){
        categoryRepository.deleteByNameAndOwnerId(categoryDTO.getUpdatedName(),userId);
    }

}
