package com.medilab.service;

import com.medilab.dto.InventoryItemDto;
import com.medilab.entity.InventoryItem;
import com.medilab.exception.ResourceNotFoundException;
import com.medilab.mapper.InventoryItemMapper;
import com.medilab.repository.InventoryItemRepository;
import com.medilab.repository.LabRepository;
import com.medilab.repository.StaffUserRepository;
import com.medilab.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final StaffUserRepository staffUserRepository;
    private final LabRepository labRepository;
    private final InventoryItemMapper inventoryItemMapper;
    private final AuditLogService auditLogService;

    public List<InventoryItemDto> getInventory() {
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return inventoryItemRepository.findByLabId(user.getLabId()).stream()
                .map(inventoryItemMapper::toDto)
                .collect(Collectors.toList());
    }

    public InventoryItemDto createInventoryItem(InventoryItemDto inventoryItemDto) {
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        InventoryItem inventoryItem = inventoryItemMapper.toEntity(inventoryItemDto);

        labRepository.findById(user.getLabId()).ifPresent(inventoryItem::setLab);
        staffUserRepository.findById(user.getId()).ifPresent(inventoryItem::setAddedBy);

        InventoryItem savedInventoryItem = inventoryItemRepository.save(inventoryItem);
        auditLogService.logAction("INVENTORY_CREATED", "Inventory item '" + savedInventoryItem.getName() + "' (ID: " + savedInventoryItem.getId() + ") was created.");
        return inventoryItemMapper.toDto(savedInventoryItem);
    }

    public InventoryItemDto updateInventoryItem(Long id, InventoryItemDto inventoryItemDto) {
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        InventoryItem existingInventoryItem = inventoryItemRepository.findByIdAndLabId(id, user.getLabId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));

        existingInventoryItem.setName(inventoryItemDto.getName());
        existingInventoryItem.setCategory(inventoryItemDto.getCategory());
        existingInventoryItem.setQuantity(inventoryItemDto.getQuantity());
        existingInventoryItem.setLowStockThreshold(inventoryItemDto.getLowStockThreshold());
        existingInventoryItem.setSupplier(inventoryItemDto.getSupplier());

        InventoryItem updatedInventoryItem = inventoryItemRepository.save(existingInventoryItem);
        auditLogService.logAction("INVENTORY_UPDATED", "Inventory item '" + updatedInventoryItem.getName() + "' (ID: " + updatedInventoryItem.getId() + ") was updated.");
        return inventoryItemMapper.toDto(updatedInventoryItem);
    }

    public void deleteInventoryItem(Long id) {
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        InventoryItem existingInventoryItem = inventoryItemRepository.findByIdAndLabId(id, user.getLabId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));

        inventoryItemRepository.deleteById(id);
        auditLogService.logAction("INVENTORY_DELETED", "Inventory item '" + existingInventoryItem.getName() + "' (ID: " + existingInventoryItem.getId() + ") was deleted.");
    }
}
