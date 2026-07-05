package com.Passman.Manager.vault.internal.Repos;


import com.Passman.Manager.vault.internal.Models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    void deleteByNameAndOwnerId(String categoryName, long OwnerId);
    @Query(value = "select * from categories c where c.name = :name and (owner_id = :ownerId or system = true) ", nativeQuery = true)
    Optional<Category> findCategoryByNameAndOwnerId(@Param("name") String name, @Param("ownerId") long ownerId);

    @Query(value = "select c.name from categories c where owner_id = :ownerId or system = true ", nativeQuery = true)
    List<String> findCategoriesByOwnerIdOrSystem(@Param("ownerId") long ownerId);
}
