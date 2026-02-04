-- Configuration Entries (The Snapshot Data)
CREATE TABLE configuration_entries (
                                       id BIGSERIAL PRIMARY KEY,
                                       config_key VARCHAR(255) NOT NULL,
                                       config_value TEXT NOT NULL
);

-- Releases (The Version Headers)
CREATE TABLE releases (
                          id BIGSERIAL PRIMARY KEY,
                          version_name VARCHAR(50),
                          region_id INT NOT NULL,
                          global_version INT,
                          status VARCHAR(20) NOT NULL,
                          created_at TIMESTAMP NOT NULL,
                          publisher VARCHAR(100)
);

-- Drafts (The Staging Area)
CREATE TABLE draft_configs (
                               config_key VARCHAR(255) PRIMARY KEY,
                               config_value TEXT NOT NULL,
                               region_id INT NOT NULL
);

-- Snapshots (The Join Table)
CREATE TABLE release_snapshots (
                                   release_id BIGINT REFERENCES releases(id),
                                   entry_id BIGINT REFERENCES configuration_entries(id),
                                   PRIMARY KEY (release_id, entry_id)
);