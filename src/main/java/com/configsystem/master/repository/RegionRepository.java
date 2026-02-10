package com.configsystem.master.repository;

import com.configsystem.master.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RegionRepository extends JpaRepository<Region, UUID> {
}
