package com.medilab.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "staff_users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StaffUser {
    @Id
    private String id;
    private String name;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String labId;

    public enum Role { Manager, Technician }
}
