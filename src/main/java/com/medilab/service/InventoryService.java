package com.medilab.service;

import com.medilab.dto.InventoryItemDto;
import com.medilab.entity.InventoryItem;
import com.medilab.exception.ResourceNotFoundException;
import com.medilab.mapper.InventoryItemMapper;
import com.medilab.repository.InventoryRepository;
import com.medilab.repository.LabRepository;
import com.medilab.repository.StaffUserRepository;
import com.medilab.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import com.medilab.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final StaffUserRepository staffUserRepository;
    private final LabRepository labRepository;
    private final InventoryItemMapper inventoryItemMapper;
    private final AuditLogService auditLogService;

    public Page<InventoryItemDto> getInventory(int page, int limit, String q, String sort, String order) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        Sort.Direction direction = Sort.Direction.fromString(order);
        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, limit, Sort.by(direction, sort));

        Specification<InventoryItem> spec = Specification
                .where((root, query, cb) -> cb.equal(root.get("lab").get("id"), user.getLabId()));

        if (StringUtils.hasText(q)) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + q.toLowerCase() + "%"));
        }

        return inventoryRepository.findAll(spec, pageable).map(inventoryItemMapper::toDto);
    }

    public InventoryItemDto createInventoryItem(InventoryItemDto inventoryItemDto) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        InventoryItem inventoryItem = inventoryItemMapper.toEntity(inventoryItemDto);

        labRepository.findById(user.getLabId()).ifPresent(inventoryItem::setLab);
        staffUserRepository.findById(user.getId()).ifPresent(inventoryItem::setAddedBy);

        InventoryItem savedInventoryItem = inventoryRepository.save(inventoryItem);
        auditLogService.logAction("INVENTORY_CREATED", "Inventory item '" + savedInventoryItem.getName() + "' (ID: "
                + savedInventoryItem.getId() + ") was created.");
        return inventoryItemMapper.toDto(savedInventoryItem);
    }

    public InventoryItemDto updateInventoryItem(Long id, InventoryItemDto inventoryItemDto) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        InventoryItem existingInventoryItem = inventoryRepository.findByIdAndLabId(id, user.getLabId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));

        existingInventoryItem.setName(inventoryItemDto.getName());
        existingInventoryItem.setCategory(inventoryItemDto.getCategory());
        existingInventoryItem.setQuantity(inventoryItemDto.getQuantity());
        existingInventoryItem.setLowStockThreshold(inventoryItemDto.getLowStockThreshold());
        existingInventoryItem.setSupplier(inventoryItemDto.getSupplier());

        InventoryItem updatedInventoryItem = inventoryRepository.save(existingInventoryItem);
        auditLogService.logAction("INVENTORY_UPDATED", "Inventory item '" + updatedInventoryItem.getName() + "' (ID: "
                + updatedInventoryItem.getId() + ") was updated.");
        return inventoryItemMapper.toDto(updatedInventoryItem);
    }

    public void deleteInventoryItem(Long id) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        InventoryItem existingInventoryItem = inventoryRepository.findByIdAndLabId(id, user.getLabId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));

        inventoryRepository.deleteById(id);
        auditLogService.logAction("INVENTORY_DELETED", "Inventory item '" + existingInventoryItem.getName() + "' (ID: "
                + existingInventoryItem.getId() + ") was deleted.");
    }
}
