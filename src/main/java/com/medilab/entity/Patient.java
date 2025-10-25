package com.medilab.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "patients")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Patient {
    @Id
    private String id;
    private String name;
    private LocalDate dob;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private String contact;
    private String createdById;
    private String labId;

    public enum Gender { Male, Female, Other }
}
