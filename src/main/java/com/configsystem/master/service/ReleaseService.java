package com.configsystem.master.service;

import com.configsystem.master.dto.ConfigEntryDTO;
import com.configsystem.master.dto.ReleaseResponse;
import com.configsystem.master.entity.*;
import com.configsystem.master.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReleaseService {

    @Autowired private DraftRepository draftRepo;
    @Autowired private ReleaseRepository releaseRepo;
    @Autowired private ConfigEntryRepository entryRepo;

    /**
     * FR-2: Commit as Release Candidate (RC)
     */
    public ReleaseResponse createReleaseCandidate(Integer regionId, String publisher) {
        List<DraftConfig> drafts = draftRepo.findByRegionId(regionId);
        if (drafts.isEmpty()) {
            throw new RuntimeException("No drafts found for region: " + regionId);
        }

        Release rc = new Release();
        rc.setRegionId(regionId);
        rc.setStatus(ReleaseStatus.RC);
        rc.setCreatedAt(LocalDateTime.now());
        rc.setPublisher(publisher);

        for (DraftConfig draft : drafts) {
            ConfigurationEntry entry = new ConfigurationEntry();
            entry.setConfigKey(draft.getConfigKey());
            entry.setConfigValue(draft.getConfigValue());
            rc.getEntries().add(entryRepo.save(entry));
        }

        Release saved = releaseRepo.save(rc);
        return getMergedReleaseResponse(saved);
    }

    /**
     * FR-3: Publish Version
     */
    public ReleaseResponse publishRelease(Long rcId) {
        Release rc = releaseRepo.findById(rcId)
                .orElseThrow(() -> new RuntimeException("Release Candidate not found."));

        if (rc.getStatus() != ReleaseStatus.RC) {
            throw new IllegalStateException("Only RCs can be published. Status is: " + rc.getStatus());
        }

        // Generate X.Y version
        int nextY = releaseRepo.findMaxGlobalVersion(rc.getRegionId()) + 1;
        rc.setGlobalVersion(nextY);
        rc.setVersionName(rc.getRegionId() + "." + nextY);

        rc.setStatus(ReleaseStatus.RELEASED);
        Release published = releaseRepo.save(rc);

        // Clear drafts after successful publish
        draftRepo.deleteByRegionId(rc.getRegionId());

        return getMergedReleaseResponse(published);
    }

    /**
     * FR-4: Reconstruct specific version
     */
    public ReleaseResponse getVersion(String versionName) {
        Release release = releaseRepo.findByVersionName(versionName)
                .orElseThrow(() -> new RuntimeException("Version " + versionName + " not found."));

        return getMergedReleaseResponse(release);
    }

    /**
     * FR-8: Logic to merge Global (0.Y) and Regional (X.Y) states.
     */
    public Map<String, String> getVersionState(String versionName) {
        String[] parts = versionName.split("\\.");
        int regionId = Integer.parseInt(parts[0]);
        int globalVer = Integer.parseInt(parts[1]);

        Map<String, String> finalMap = new HashMap<>();

        // 1. Inherit from Global (0.Y) if we are in a region
        if (regionId != 0) {
            String globalName = releaseRepo.findLatestGlobalVersionName();
            finalMap.putAll(fetchRawMapFromDb(globalName));
        }

        // 2. Overlay Regional (X.Y) entries
        finalMap.putAll(fetchRawMapFromDb(versionName));

        return finalMap;
    }

    /**
     * Helper: Maps a Release Entity into a Merged DTO Response.
     * Uses Java 21 .toList() for cleaner syntax.
     */
    private ReleaseResponse getMergedReleaseResponse(Release release) {
        Map<String, String> mergedData;

        // If RELEASED, we can perform the full X.Y inheritance merge
        if (release.getStatus() == ReleaseStatus.RELEASED && release.getVersionName() != null) {
            mergedData = getVersionState(release.getVersionName());
        } else {
            // For RC, we show only what is currently inside the bundle
            mergedData = release.getEntries().stream()
                    .collect(Collectors.toMap(
                            ConfigurationEntry::getConfigKey,
                            ConfigurationEntry::getConfigValue,
                            (a, b) -> b));
        }

        List<ConfigEntryDTO> entryDTOs = mergedData.entrySet().stream()
                .map(e -> new ConfigEntryDTO(e.getKey(), e.getValue()))
                .toList(); // Java 21 syntax

        return new ReleaseResponse(
                release.getId(),
                release.getVersionName(),
                release.getRegionId(),
                release.getStatus().name(),
                release.getPublisher(),
                entryDTOs
        );
    }

    private Map<String, String> fetchRawMapFromDb(String versionName) {
        return releaseRepo.findByVersionName(versionName)
                .map(r -> r.getEntries().stream()
                        .collect(Collectors.toMap(
                                ConfigurationEntry::getConfigKey,
                                ConfigurationEntry::getConfigValue,
                                (a, b) -> b)))
                .orElse(Map.of());
    }
}