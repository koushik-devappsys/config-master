package com.configsystem.master.repository;

import com.configsystem.master.entity.Regulation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RegulationReporitory extends JpaRepository<Regulation, UUID> {
}
