package com.configsystem.master.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "rights")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Right {
    @Id
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regulation_uuid")
    private Regulation regulation;

    @Column(columnDefinition = "TEXT")
    private String description;
}