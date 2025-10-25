package com.medilab.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventory")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryItem {
    @Id
    private String id;
    private String name;
    private String category;
    private Integer quantity;
    private Integer lowStockThreshold;
    private String supplier;
    private String addedById;
    private String labId;
}
