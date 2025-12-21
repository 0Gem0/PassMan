package com.Passman.Manager.Vault.Repos;

import com.Passman.Manager.Vault.DTO.CategoryCountDTO;
import com.Passman.Manager.Vault.Models.Entry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@Repository
public interface EntryRepository extends JpaRepository<Entry,Long> {

     //В vault уже ищу по категории записи
     List<Entry> findAllByCategoryIdAndUserId(long categoryId, long userId);


     @Query(value = "select count(e.id) from entries e where e.user_id = :userId", nativeQuery = true)
     Long findCountAll(@Param("userId") long userId);

     List<Entry> findAllByUserId(long userId);
     Entry findByCategoryNameAndTitleAndUserId(String categoryName, String title, long userId);

     @Query(
             value = "SELECT c.name as categoryName, count(e.id) as countEntries " +
                     "FROM entries e JOIN categories c on e.category_id = c.id " +
                     "WHERE e.user_id = :userId " +
                     "GROUP BY c.name",
             nativeQuery = true)
     List<CategoryCountDTO> findCategoryCountsByUserId(@Param("userId") long userId);

     Optional<Entry> findEntryById(long id);


}
