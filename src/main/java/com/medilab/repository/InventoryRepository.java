package com.medilab.repository;

import com.medilab.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryRepository extends JpaRepository<InventoryItem, String> {
    List<InventoryItem> findAllByLabId(String labId);
}
