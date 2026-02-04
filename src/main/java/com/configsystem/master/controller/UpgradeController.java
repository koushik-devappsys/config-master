package com.configsystem.master.controller;

import com.configsystem.master.dto.UpgradePackage;
import com.configsystem.master.service.UpgradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/upgrade")
@Tag(name = "Upgrade Engine", description = "Generates delta packages to move between versions")
public class UpgradeController {

    @Autowired
    private UpgradeService upgradeService;

    @Operation(summary = "Generate upgrade path between two versions",
            description = "Calculates the ADD, UPDATE, and DELETE operations needed to move from one version to another.")
    @GetMapping
    public UpgradePackage getUpgradePath(
            @RequestParam String from,
            @RequestParam String to) {
        return upgradeService.generateUpgradePath(from, to);
    }
}