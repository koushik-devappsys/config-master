package com.configsystem.master.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "regulations")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Regulation {
    @Id
    private UUID uuid;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @OneToMany(mappedBy = "regulation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Right> rights;
}