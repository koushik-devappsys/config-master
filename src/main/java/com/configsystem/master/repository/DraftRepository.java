package com.configsystem.master.repository;

import com.configsystem.master.entity.DraftConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DraftRepository extends JpaRepository<DraftConfig, String> {

    List<DraftConfig> findByRegionId(Integer regionId);

    @Modifying
    void deleteByRegionId(Integer regionId);
}