package com.configsystem.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.SQLType;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "version_row_snapshots")
@Getter @Setter
public class VersionRowSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String versionName;
    private UUID regionId;
    private String tableName;
    private UUID rowUuid;

    private String changeType;
    private LocalDateTime createdAt=LocalDateTime.now();
    private String createdBy;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String,Object> row_data_json;
}
