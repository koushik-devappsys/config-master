package com.configsystem.master.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "pd_elements")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdElement {
    @Id
    private UUID uuid;

    @Column(nullable = false)
    private String name;

    @Column(name = "is_deleted")
    private int isDeleted = 0;
}