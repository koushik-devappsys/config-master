package com.configsystem.master.repository;

import com.configsystem.master.entity.VersionRowSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VersionRowSnapshotRepository extends JpaRepository<VersionRowSnapshot,Long> {
}
