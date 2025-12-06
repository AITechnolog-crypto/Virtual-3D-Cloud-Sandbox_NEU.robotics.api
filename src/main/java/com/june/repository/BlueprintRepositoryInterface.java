package com.june.repository;

import com.june.model.Blueprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlueprintRepository extends JpaRepository<Blueprint, Long> {
    
    Optional<Blueprint> findByNameAndVersion(String name, String version);
    List<Blueprint> findByCategory(String category);
    List<Blueprint> findByStatus(String status);
    List<Blueprint> findByCreatedBy(String createdBy);
    
    @Query("SELECT b FROM Blueprint b WHERE b.name = :name " +
           "ORDER BY b.version DESC LIMIT 1")
    Optional<Blueprint> findLatestVersion(String name);
    
    List<Blueprint> findByNameOrderByVersionDesc(String name);
    
    @Query("SELECT b FROM Blueprint b WHERE " +
           "LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Blueprint> searchBlueprints(String keyword);
    
    @Query("SELECT b.category, COUNT(b) FROM Blueprint b GROUP BY b.category")
    List<Object[]> countByCategory();
    
    List<Blueprint> findByStatusIn(List<String> statuses);
}
