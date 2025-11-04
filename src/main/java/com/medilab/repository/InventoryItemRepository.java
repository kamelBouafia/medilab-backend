package com.medilab.repository;

import com.medilab.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    List<InventoryItem> findByLabId(Long labId);

    Optional<InventoryItem> findByIdAndLabId(Long id, Long labId);
}
