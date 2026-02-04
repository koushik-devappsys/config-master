package com.configsystem.master.controller;

import com.configsystem.master.dto.ReleaseResponse;
import com.configsystem.master.service.ReleaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/releases")
@Tag(name = "Release Management", description = "Operations related to committing and publishing configuration versions")
public class ReleaseController {

    @Autowired
    private ReleaseService releaseService;

    // FR-2: Commit drafts
    @Operation(summary = "Commit drafts as a Release Candidate",
            description = "Takes all isolated drafts for a region and bundles them into a temporary RC state.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully committed"),
            @ApiResponse(responseCode = "400", description = "No drafts found to commit")
    })
    @PostMapping("/commit")
    public ReleaseResponse commit(@RequestParam Integer regionId, @RequestParam String user) {
        return releaseService.createReleaseCandidate(regionId, user);
    }

    // FR-3: Publish and generate X.Y version
    @PostMapping("/{id}/publish")
    public ReleaseResponse publish(@PathVariable Long id) {
        return releaseService.publishRelease(id);
    }

    // FR-4: Reconstruct specific version
    @GetMapping("/{versionName}")
    public ReleaseResponse getByVersion(@PathVariable String versionName) {
        return releaseService.getVersion(versionName);
    }
}