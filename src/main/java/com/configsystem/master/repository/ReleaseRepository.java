package com.configsystem.master.repository;

import com.configsystem.master.entity.Release;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ReleaseRepository extends JpaRepository<Release, Long> {

    Optional<Release> findByVersionName(String versionName);

    @Query("SELECT COALESCE(MAX(r.globalVersion), 0) FROM Release r WHERE r.regionId = ?1")
    int findMaxGlobalVersion(Integer regionId);

    @Query(value = "SELECT version_name FROM releases WHERE region_id = 0 AND status = 'RELEASED' ORDER BY global_version DESC LIMIT 1", nativeQuery = true)
    String findLatestGlobalVersionName();
}