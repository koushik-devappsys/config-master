package com.configsystem.master.service;

import com.configsystem.master.dto.ConfigEntryDTO;
import com.configsystem.master.dto.ReleaseResponse;
import com.configsystem.master.entity.*;
import com.configsystem.master.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional // Ensures Atomicity: all steps succeed or all fail
public class ReleaseService {

    @Autowired private DraftRepository draftRepo;
    @Autowired private ReleaseRepository releaseRepo;
    @Autowired private ConfigEntryRepository entryRepo;

    /**
     * FR-2: Commit as Release Candidate (RC)
     * Takes staged changes and creates an editable but grouped bundle.
     */
    public ReleaseResponse createReleaseCandidate(Integer regionId, String publisher) {
        // 1. Fetch drafts for the region
        List<DraftConfig> drafts = draftRepo.findByRegionId(regionId);
        if (drafts.isEmpty()) {
            throw new RuntimeException("No drafts found for region: " + regionId);
        }

        // 2. Create the Release object
        Release rc = new Release();
        rc.setRegionId(regionId);
        rc.setStatus(ReleaseStatus.RC);
        rc.setCreatedAt(LocalDateTime.now());
        rc.setPublisher(publisher);

        // 3. Snapshot: Copy data from Draft to Immutable ConfigurationEntry rows
        for (DraftConfig draft : drafts) {
            ConfigurationEntry entry = new ConfigurationEntry();
            entry.setConfigKey(draft.getConfigKey());
            entry.setConfigValue(draft.getConfigValue());

            // Link the snapshot to this specific RC
            rc.getEntries().add(entryRepo.save(entry));
        }

        Release saved = releaseRepo.save(rc);
        return mapToResponse(saved);
    }

    /**
     * FR-3: Publish Version (Finalize)
     * Generates X.Y version name, changes status to RELEASED, and clears drafts.
     */
    public ReleaseResponse publishRelease(Long rcId) {
        // 1. Find the RC
        Release rc = releaseRepo.findById(rcId)
                .orElseThrow(() -> new RuntimeException("RC ID " + rcId + " not found."));

        // 2. Business Rule: Only RC can be published
        if (rc.getStatus() != ReleaseStatus.RC) {
            throw new IllegalStateException("Only Release Candidates can be published. Current status: " + rc.getStatus());
        }

        // 3. Versioning Logic: X.Y (Section 3 of BRD)
        // Find current max Y for this region (X) and increment by 1
        int nextY = releaseRepo.findMaxGlobalVersion(rc.getRegionId()) + 1;
        rc.setGlobalVersion(nextY);
        rc.setVersionName(rc.getRegionId() + "." + nextY);

        // 4. Update Status to RELEASED (Makes it Immutable)
        rc.setStatus(ReleaseStatus.RELEASED);

        // 5. Cleanup: Delete Drafts once they are successfully published
        draftRepo.deleteByRegionId(rc.getRegionId());

        Release published = releaseRepo.save(rc);
        return mapToResponse(published);
    }

    /**
     * FR-4: Database State Reconstruction
     * Retrieves the exact state of any version.
     */
    public ReleaseResponse getVersion(String versionName) {
        Release release = releaseRepo.findByVersionName(versionName)
                .orElseThrow(() -> new RuntimeException("Version " + versionName + " not found."));

        return mapToResponse(release);
    }

    /**
     * Helper Method: Maps Entity (DB structure) to DTO (API Response)
     */
    private ReleaseResponse mapToResponse(Release release) {
        List<ConfigEntryDTO> entries = release.getEntries().stream()
                .map(e -> new ConfigEntryDTO(e.getConfigKey(), e.getConfigValue()))
                .collect(Collectors.toList());

        return new ReleaseResponse(
                release.getId(),
                release.getVersionName(),
                release.getRegionId(),
                release.getStatus().name(),
                release.getPublisher(),
                entries
        );
    }
}