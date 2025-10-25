package com.medilab.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "labs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Lab {
    @Id
    private String id;
    private String name;
}
