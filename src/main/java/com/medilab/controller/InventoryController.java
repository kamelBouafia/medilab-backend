package com.medilab.controller;

import com.medilab.dto.InventoryItemDto;
import com.medilab.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@PreAuthorize("hasRole('Staff')")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<InventoryItemDto>> getInventory() {
        return ResponseEntity.ok(inventoryService.getInventory());
    }

    @PostMapping
    public ResponseEntity<InventoryItemDto> createInventoryItem(@RequestBody InventoryItemDto inventoryItemDto) {
        InventoryItemDto createdItem = inventoryService.createInventoryItem(inventoryItemDto);
        return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryItemDto> updateInventoryItem(@PathVariable Long id, @RequestBody InventoryItemDto inventoryItemDto) {
        InventoryItemDto updatedItem = inventoryService.updateInventoryItem(id, inventoryItemDto);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<Void> deleteInventoryItem(@PathVariable Long id) {
        inventoryService.deleteInventoryItem(id);
        return ResponseEntity.noContent().build();
    }
}
