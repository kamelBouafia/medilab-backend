package com.medilab.dto;

import lombok.Data;

@Data
public class InventoryItemDto {
    private Long id;
    private String name;
    private String category;
    private int quantity;
    private int lowStockThreshold;
    private String supplier;
    private Long addedById;
    private String labName;
}
