package com.configsystem.master.repository;

import com.configsystem.master.entity.Right;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RightRepository extends JpaRepository<Right, UUID> {
}
