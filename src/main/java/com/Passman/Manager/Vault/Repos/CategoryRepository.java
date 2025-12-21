package com.Passman.Manager.Vault.Repos;


import com.Passman.Manager.Vault.Models.Category;
import org.apache.catalina.LifecycleState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {


    @Query(value = "select * from categories c where c.name = :name and (owner_id = :ownerId or system = true) ", nativeQuery = true)
    Category findCategoryByNameAndOwnerId(@Param("name") String name,@Param("ownerId") long ownerId);

    @Query(value = "select c.name from categories c where owner_id = :ownerId or system = true ", nativeQuery = true)
    List<String> findCategoriesByOwnerIdOrSystem(@Param("ownerId") long ownerId);
}
