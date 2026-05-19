package com.Passman.Manager.vault.internal.Repos;

import com.Passman.Manager.vault.DTO.CategoryCountDTO;

import com.Passman.Manager.vault.internal.Models.Entry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntryRepository extends JpaRepository<Entry, Long> {

    List<Entry> findAllByCategoryIdAndUserId(long categoryId, long userId);

    void deleteById(long id);

    @Query(value = "SELECT COUNT(e.id) FROM entries e WHERE e.user_id = :userId", nativeQuery = true)
    Long findCountAll(@Param("userId") long userId);

    @Query(value = """
        SELECT DISTINCT e.*
        FROM entries e
        WHERE e.category_id = (SELECT c.id FROM categories c WHERE c.name = :categoryName)
          AND (
            EXISTS (
                SELECT 1
                FROM useraccessrights uar
                WHERE uar.entry_id = e.id
                  AND uar.user_id = :userId
                  AND (uar.can_view = true OR uar.can_edit = true)
            )
            OR (
                NOT EXISTS (
                    SELECT 1
                    FROM useraccessrights uar2
                    WHERE uar2.entry_id = e.id
                      AND uar2.user_id = :userId
                )
                AND EXISTS (
                    SELECT 1
                    FROM accessrights ar
                    JOIN roles r ON ar.role_id = r.id
                    JOIN users_role ur ON r.id = ur.role_id
                    WHERE ar.entry_id = e.id
                      AND ur.user_id = :userId
                      AND (ar.can_view = true OR ar.can_edit = true)
                )
            )
          )
    """, nativeQuery = true)
    List<Entry> findAccessibleEntriesByCategory(@Param("userId") Long userId,
                                                @Param("categoryName") String categoryName);

    @Query(value = """
        SELECT COUNT(DISTINCT e.id)
        FROM entries e
        WHERE
            EXISTS (
                SELECT 1
                FROM useraccessrights uar
                WHERE uar.entry_id = e.id
                  AND uar.user_id = :userId
                  AND (uar.can_view = true OR uar.can_edit = true)
            )
            OR (
                NOT EXISTS (
                    SELECT 1
                    FROM useraccessrights uar2
                    WHERE uar2.entry_id = e.id
                      AND uar2.user_id = :userId
                )
                AND EXISTS (
                    SELECT 1
                    FROM accessrights ar
                    JOIN roles r ON ar.role_id = r.id
                    JOIN users_role ur ON r.id = ur.role_id
                    WHERE ar.entry_id = e.id
                      AND ur.user_id = :userId
                      AND (ar.can_view = true OR ar.can_edit = true)
                )
            )
    """, nativeQuery = true)
    Long countAccessibleEntries(@Param("userId") Long userId);

    @Query(value = """
        SELECT DISTINCT e.*
        FROM entries e
        WHERE
            EXISTS (
                SELECT 1
                FROM useraccessrights uar
                WHERE uar.entry_id = e.id
                  AND uar.user_id = :userId
                  AND (uar.can_view = true OR uar.can_edit = true)
            )
            OR
            EXISTS (
                SELECT 1
                FROM accessrights ar
                JOIN roles r ON ar.role_id = r.id
                JOIN users_role ur ON r.id = ur.role_id
                WHERE ar.entry_id = e.id
                  AND ur.user_id = :userId
                  AND (ar.can_view = true OR ar.can_edit = true)
            )
    """, nativeQuery = true)
    List<Entry> findAccessibleEntries(@Param("userId") Long userId);

    @Query(value = """
        SELECT c.name as category_name, COUNT(e.id) as count_entries
        FROM entries e 
        JOIN categories c ON e.category_id = c.id 
        WHERE e.user_id = :userId 
        GROUP BY c.name
    """, nativeQuery = true)
    List<CategoryCountDTO> findCategoryCountsByUserId(@Param("userId") long userId);

    Optional<Entry> findEntryById(long id);
}