package com.medilab.controller;

import com.medilab.dto.InventoryItemDto;
import com.medilab.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@PreAuthorize("hasRole('Staff')")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<InventoryItemDto>> getInventory(
            @RequestParam(defaultValue = "0") int _page,
            @RequestParam(defaultValue = "10") int _limit,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "id") String _sort,
            @RequestParam(defaultValue = "asc") String _order) {
        Page<InventoryItemDto> inventoryPage = inventoryService.getInventory(_page, _limit, q, _sort, _order);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(inventoryPage.getTotalElements()));
        return ResponseEntity.ok().headers(headers).body(inventoryPage.getContent());
    }

    @PostMapping
    public ResponseEntity<InventoryItemDto> createInventoryItem(@Valid @RequestBody InventoryItemDto inventoryItemDto) {
        InventoryItemDto createdItem = inventoryService.createInventoryItem(inventoryItemDto);
        return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryItemDto> updateInventoryItem(@PathVariable Long id, @Valid @RequestBody InventoryItemDto inventoryItemDto) {
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
