package com.configsystem.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "releases")
@Getter @Setter
public class Release {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String versionName;
    private Integer regionId;
    private Integer globalVersion;

    @Enumerated(EnumType.STRING) // This is critical for Enums
    private ReleaseStatus status;

    private LocalDateTime createdAt;
    private String publisher;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "release_snapshots",
            joinColumns = @JoinColumn(name = "release_id"),
            inverseJoinColumns = @JoinColumn(name = "entry_id")
    )
    private Set<ConfigurationEntry> entries = new HashSet<>();
}