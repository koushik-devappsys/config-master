package com.configsystem.master.repository;

import com.configsystem.master.entity.ConfigurationEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigEntryRepository extends JpaRepository<ConfigurationEntry,Long> {
}