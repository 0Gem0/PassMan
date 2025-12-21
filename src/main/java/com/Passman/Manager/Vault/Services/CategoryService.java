package com.Passman.Manager.Vault.Services;


import com.Passman.Manager.Vault.Models.Category;
import com.Passman.Manager.Vault.Repos.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

}
