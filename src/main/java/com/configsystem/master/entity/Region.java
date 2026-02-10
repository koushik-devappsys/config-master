package com.configsystem.master.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "regions")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Region {
    @Id
    private UUID id;

    private String name;

    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL)
    private List<Regulation> regulations;
}