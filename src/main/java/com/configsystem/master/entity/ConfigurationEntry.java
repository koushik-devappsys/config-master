package com.configsystem.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "configuration_entries")
@Getter
@Setter
public class ConfigurationEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String configKey;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String configValue;
}