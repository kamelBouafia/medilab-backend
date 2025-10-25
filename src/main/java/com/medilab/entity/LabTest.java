package com.medilab.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "lab_tests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LabTest {
    @Id
    private String id;
    private String name;
    private String category;
    private String labId;
}
