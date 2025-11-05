package com.medilab.repository;

import com.medilab.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<InventoryItem, Long>, JpaSpecificationExecutor<InventoryItem> {
    List<InventoryItem> findByLabId(Long labId);
    Optional<InventoryItem> findByIdAndLabId(Long id, Long labId);

    @Query("SELECT count(i) FROM InventoryItem i WHERE i.lab.id = :labId AND i.quantity < i.lowStockThreshold")
    long countByLabIdAndQuantityLessThanLowStockThreshold(@Param("labId") Long labId);
}
