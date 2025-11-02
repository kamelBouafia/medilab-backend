package com.medilab.service;

import com.medilab.dto.InventoryItemDto;
import com.medilab.entity.InventoryItem;
import com.medilab.entity.StaffUser;
import com.medilab.mapper.InventoryItemMapper;
import com.medilab.repository.InventoryItemRepository;
import com.medilab.repository.LabRepository;
import com.medilab.repository.StaffUserRepository;
import com.medilab.security.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private StaffUserRepository staffUserRepository;

    @Autowired
    private LabRepository labRepository;

    @Autowired
    private InventoryItemMapper inventoryItemMapper;

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
        return inventoryItemMapper.toDto(savedInventoryItem);
    }

    public InventoryItemDto updateInventoryItem(Long id, InventoryItemDto inventoryItemDto) {
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Add logic to ensure the item belongs to the user's lab
        InventoryItem inventoryItem = inventoryItemMapper.toEntity(inventoryItemDto);
        inventoryItem.setId(id);

        labRepository.findById(user.getLabId()).ifPresent(inventoryItem::setLab);
        staffUserRepository.findById(user.getId()).ifPresent(inventoryItem::setAddedBy);

        InventoryItem updatedInventoryItem = inventoryItemRepository.save(inventoryItem);
        return inventoryItemMapper.toDto(updatedInventoryItem);
    }

    public void deleteInventoryItem(Long id) {
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Add logic to ensure the item belongs to the user's lab
        inventoryItemRepository.deleteById(id);
    }
}
