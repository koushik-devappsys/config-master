package com.configsystem.master.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "draft_configs")
@Getter @Setter
public class DraftConfig {
    @Id
    private String configKey;
    private String configValue;
    private Integer regionId;
}