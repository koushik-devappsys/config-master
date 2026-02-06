package com.configsystem.master.service;

import com.configsystem.master.dto.ConfigEntryDTO;
import com.configsystem.master.dto.ReleaseResponse;
import com.configsystem.master.entity.*;
import com.configsystem.master.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReleaseService {

    @Autowired private DraftRepository draftRepo;
    @Autowired private ReleaseRepository releaseRepo;
    @Autowired private ConfigEntryRepository entryRepo;

    /**
     * FR-2: Commit as Release Candidate (RC)
     * CUMULATIVE: Loads 0.4 entries when creating 0.5.
     * VALIDATION: Regions cannot add keys missing from Global.
     */
    public ReleaseResponse createReleaseCandidate(Integer regionId, String publisher) {
        List<DraftConfig> drafts = draftRepo.findByRegionId(regionId);
        if (drafts.isEmpty()) throw new RuntimeException("No drafts found to commit.");

        // RULE: Global owns the schema. Regions (X>0) cannot add new keys.
        if (regionId != 0) {
            validateRegionalDraftsAgainstGlobalSchema(drafts);
        }

        // CUMULATIVE: Start with all entries from the previous version of THIS region
        Map<String, ConfigurationEntry> snapshotMap = getLatestSnapshotMap(regionId);

        // APPLY DRAFTS: Update values in our map
        for (DraftConfig draft : drafts) {
            ConfigurationEntry entry = new ConfigurationEntry();
            entry.setConfigKey(draft.getConfigKey());
            entry.setConfigValue(draft.getConfigValue());

            // Overwrite existing or add new (new only allowed for Global due to validation above)
            snapshotMap.put(draft.getConfigKey(), entryRepo.save(entry));
        }

        Release rc = new Release();
        rc.setRegionId(regionId);
        rc.setStatus(ReleaseStatus.RC);
        rc.setCreatedAt(LocalDateTime.now());
        rc.setPublisher(publisher);
        rc.setEntries(new HashSet<>(snapshotMap.values()));

        Release saved = releaseRepo.save(rc);
        return getMergedReleaseResponse(saved);
    }

    /**
     * FR-3: Publish Version
     * CRITICAL CHECK: Ensures an RC is only published once.
     */
    public ReleaseResponse publishRelease(Long rcId) {
        Release rc = releaseRepo.findById(rcId)
                .orElseThrow(() -> new RuntimeException("Release Candidate not found with ID: " + rcId));

        // THE FIX: This prevents multiple clicks from incrementing the version incorrectly
        if (rc.getStatus() != ReleaseStatus.RC) {
            throw new IllegalStateException("Cannot publish. This release is already in status: " + rc.getStatus());
        }

        // Assign Version Name (X.Y)
        int nextY = releaseRepo.findMaxGlobalVersion(rc.getRegionId()) + 1;
        rc.setGlobalVersion(nextY);
        rc.setVersionName(rc.getRegionId() + "." + nextY);

        // Finalize
        rc.setStatus(ReleaseStatus.RELEASED);
        Release published = releaseRepo.save(rc);

        // Cleanup Drafts
        draftRepo.deleteByRegionId(rc.getRegionId());

        return getMergedReleaseResponse(published);
    }

    /**
     * FR-4: API reconstruction
     */
    public ReleaseResponse getVersion(String versionName) {
        Release release = releaseRepo.findByVersionName(versionName)
                .orElseThrow(() -> new RuntimeException("Version " + versionName + " not found."));
        return getMergedReleaseResponse(release);
    }

    /**
     * FR-8: The Inheritance Merge Engine (Global 0.Y + Region X.Y)
     */
    public Map<String, String> getVersionState(String versionName) {
        String[] parts = versionName.split("\\.");
        int regionId = Integer.parseInt(parts[0]);
        int globalVer = Integer.parseInt(parts[1]);

        Map<String, String> finalMap = new HashMap<>();

        if (regionId != 0) {
            finalMap.putAll(fetchRawMap("0." + globalVer));
        }
        finalMap.putAll(fetchRawMap(versionName));
        return finalMap;
    }

    private void validateRegionalDraftsAgainstGlobalSchema(List<DraftConfig> drafts) {
        String latestGlobalName = releaseRepo.findLatestGlobalVersionName();
        if (latestGlobalName == null) throw new RuntimeException("Global Schema (0.x) must be published first.");

        Map<String, String> globalSchema = fetchRawMap(latestGlobalName);
        for (DraftConfig draft : drafts) {
            if (!globalSchema.containsKey(draft.getConfigKey())) {
                throw new RuntimeException("Access Denied: Key '" + draft.getConfigKey() + "' is not in the Global Schema.");
            }
        }
    }

    private Map<String, ConfigurationEntry> getLatestSnapshotMap(Integer regionId) {
        return releaseRepo.findLatestReleasedByRegion(regionId)
                .map(r -> r.getEntries().stream()
                        .collect(Collectors.toMap(ConfigurationEntry::getConfigKey, e -> e, (a, b) -> b)))
                .orElse(new HashMap<>());
    }

    private ReleaseResponse getMergedReleaseResponse(Release release) {
        Map<String, String> data;
        // Logic: RC shows its current bundle, RELEASED shows inherited state
        if (release.getStatus() == ReleaseStatus.RELEASED && release.getVersionName() != null) {
            data = getVersionState(release.getVersionName());
        } else {
            data = release.getEntries().stream()
                    .collect(Collectors.toMap(ConfigurationEntry::getConfigKey, ConfigurationEntry::getConfigValue, (a, b) -> b));
        }

        List<ConfigEntryDTO> entries = data.entrySet().stream()
                .map(e -> new ConfigEntryDTO(e.getKey(), e.getValue()))
                .toList();

        return new ReleaseResponse(release.getId(), release.getVersionName(), release.getRegionId(),
                release.getStatus().name(), release.getPublisher(), entries);
    }

    private Map<String, String> fetchRawMap(String versionName) {
        return releaseRepo.findByVersionName(versionName)
                .map(r -> r.getEntries().stream().collect(Collectors.toMap(ConfigurationEntry::getConfigKey, ConfigurationEntry::getConfigValue, (a, b) -> b)))
                .orElse(Map.of());
    }
}