-- Region Table
CREATE TABLE regions(
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

-- PD Elements
CREATE TABLE pd_elements(
    uuid UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    is_deleted INT DEFAULT 0
);

-- Regulations
CREATE TABLE regulations(
    uuid UUID PRIMARY KEY,
    region_id UUID REFERENCES regions(id),
    name VARCHAR(225) NOT NULL
);

-- Rights
CREATE TABLE rights(
    uuid UUID PRIMARY KEY,
    regulation_uuid UUID REFERENCES regulations(uuid),
    description TEXT NOT NULL
);

-- Master Version Snapshot
CREATE TABLE version_row_snapshots(
    id BIGSERIAL PRIMARY KEY,
    version_name VARCHAR(50) NOT NULL ,
    region_id UUID NOT NULL,

    table_name varchar(100) NOT NULL ,
    row_uuid UUID NOT NULL,

    change_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),

    row_data_json JSONB
);

CREATE INDEX idx_snapshot_lookup ON version_row_snapshots(region_id, version_name, row_uuid);

