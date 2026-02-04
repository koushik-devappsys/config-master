package com.configsystem.master.controller;

import com.configsystem.master.dto.ReleaseResponse;
import com.configsystem.master.service.ReleaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/releases")
public class ReleaseController {

    @Autowired
    private ReleaseService releaseService;

    // FR-2: Commit drafts
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