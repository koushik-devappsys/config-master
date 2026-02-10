package com.configsystem.master.repository;

import com.configsystem.master.entity.PdElement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PdElementRepository extends JpaRepository<PdElement, UUID> {
}
