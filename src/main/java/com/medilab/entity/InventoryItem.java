package com.medilab.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category;
    private int quantity;

    @Column(name = "lowStockThreshold")
    private int lowStockThreshold;

    private String supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addedById", nullable = false)
    private StaffUser addedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "labId", nullable = false)
    private Lab lab;
}
