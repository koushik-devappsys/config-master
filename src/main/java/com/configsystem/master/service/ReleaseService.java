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
     * Groups current drafts into a temporary, editable bundle.
     */
    public ReleaseResponse createReleaseCandidate(Integer regionId, String publisher) {
        List<DraftConfig> drafts = draftRepo.findByRegionId(regionId);
        if (drafts.isEmpty()) {
            throw new RuntimeException("No drafts found to commit for region: " + regionId);
        }

        Release rc = new Release();
        rc.setRegionId(regionId);
        rc.setStatus(ReleaseStatus.RC);
        rc.setCreatedAt(LocalDateTime.now());
        rc.setPublisher(publisher);

        // Snapshot: Create persistent entries from current drafts
        for (DraftConfig draft : drafts) {
            ConfigurationEntry entry = new ConfigurationEntry();
            entry.setConfigKey(draft.getConfigKey());
            entry.setConfigValue(draft.getConfigValue());
            rc.getEntries().add(entryRepo.save(entry));
        }

        Release saved = releaseRepo.save(rc);
        return mapToMergedResponse(saved);
    }

    /**
     * FR-3: Publish Version
     * Assigns the X.Y version, makes data immutable, and clears the staging area.
     */
    public ReleaseResponse publishRelease(Long rcId) {
        Release rc = releaseRepo.findById(rcId)
                .orElseThrow(() -> new RuntimeException("Release Candidate not found."));

        if (rc.getStatus() != ReleaseStatus.RC) {
            throw new IllegalStateException("Only Release Candidates can be published.");
        }

        // Logic for X.Y: Increment the release index (Y) for this specific region (X)
        int nextY = releaseRepo.findMaxGlobalVersion(rc.getRegionId()) + 1;
        rc.setGlobalVersion(nextY);
        rc.setVersionName(rc.getRegionId() + "." + nextY);

        rc.setStatus(ReleaseStatus.RELEASED);
        Release published = releaseRepo.save(rc);

        // FR-1: Clear drafts after they are permanently versioned
        draftRepo.deleteByRegionId(rc.getRegionId());

        return mapToMergedResponse(published);
    }

    /**
     * FR-4: Reconstruct specific version
     */
    public ReleaseResponse getVersion(String versionName) {
        Release release = releaseRepo.findByVersionName(versionName)
                .orElseThrow(() -> new RuntimeException("Version " + versionName + " not found."));

        return mapToMergedResponse(release);
    }

    /**
     * FR-8: The Inheritance Engine.
     * Reconstructs the state by layering Regional (X.Y) over Global (0.Y).
     */
    public Map<String, String> getVersionState(String versionName) {
        String[] parts = versionName.split("\\.");
        int regionId = Integer.parseInt(parts[0]);
        int globalVer = Integer.parseInt(parts[1]);

        Map<String, String> mergedConfig = new HashMap<>();

        // 1. If this is a Regional version (X > 0), load the Global Baseline (0.Y) first
        if (regionId != 0) {
            String globalVersionName = "0." + globalVer;
            mergedConfig.putAll(fetchRawData(globalVersionName));
        }

        // 2. Load the Regional Overrides (X.Y). Matches here will overwrite Global values.
        mergedConfig.putAll(fetchRawData(versionName));

        return mergedConfig;
    }

    /**
     * Helper: Unified Mapper to return the "Full Truth" (Merged Global + Regional)
     */
    private ReleaseResponse mapToMergedResponse(Release release) {
        Map<String, String> effectiveEntries;

        // If the version is not yet published (RC), we only show its specific contents.
        // If it IS published, we show the full inherited state (Global + Regional).
        if (release.getStatus() == ReleaseStatus.RELEASED && release.getVersionName() != null) {
            effectiveEntries = getVersionState(release.getVersionName());
        } else {
            effectiveEntries = release.getEntries().stream()
                    .collect(Collectors.toMap(
                            ConfigurationEntry::getConfigKey,
                            ConfigurationEntry::getConfigValue,
                            (existing, replacement) -> replacement));
        }

        List<ConfigEntryDTO> entryDTOs = effectiveEntries.entrySet().stream()
                .map(e -> new ConfigEntryDTO(e.getKey(), e.getValue()))
                .toList(); // Java 21

        return new ReleaseResponse(
                release.getId(),
                release.getVersionName(),
                release.getRegionId(),
                release.getStatus().name(),
                release.getPublisher(),
                entryDTOs
        );
    }

    /**
     * Helper: Fetches raw key-value pairs from the snapshot table
     */
    private Map<String, String> fetchRawData(String versionName) {
        return releaseRepo.findByVersionName(versionName)
                .map(r -> r.getEntries().stream()
                        .collect(Collectors.toMap(
                                ConfigurationEntry::getConfigKey,
                                ConfigurationEntry::getConfigValue,
                                (a, b) -> b)))
                .orElse(Map.of());
    }
}