package com.medilab.service;

import com.medilab.entity.InventoryItem;
import com.medilab.repository.InventoryItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;

    @Autowired
    public InventoryService(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
    }

    public List<InventoryItem> getInventory() {
        return inventoryItemRepository.findAll();
    }

    public InventoryItem addInventoryItem(InventoryItem inventoryItem) {
        return inventoryItemRepository.save(inventoryItem);
    }

    public InventoryItem updateInventoryItem(String id, InventoryItem inventoryItem) {
        // Additional logic to ensure the ID is respected
        inventoryItem.setId(id);
        return inventoryItemRepository.save(inventoryItem);
    }

    public void deleteInventoryItem(String id) {
        inventoryItemRepository.deleteById(id);
    }
}
