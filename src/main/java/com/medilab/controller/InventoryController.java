package com.medilab.controller;

import com.medilab.entity.InventoryItem;
import com.medilab.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<List<InventoryItem>> getInventory() {
        return ResponseEntity.ok(inventoryService.getInventory());
    }

    @PostMapping
    public ResponseEntity<InventoryItem> addInventoryItem(@RequestBody InventoryItem inventoryItem) {
        return ResponseEntity.ok(inventoryService.addInventoryItem(inventoryItem));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryItem> updateInventoryItem(@PathVariable String id, @RequestBody InventoryItem inventoryItem) {
        return ResponseEntity.ok(inventoryService.updateInventoryItem(id, inventoryItem));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventoryItem(@PathVariable String id) {
        inventoryService.deleteInventoryItem(id);
        return ResponseEntity.noContent().build();
    }
}
