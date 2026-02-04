package com.configsystem.master.service;

import com.configsystem.master.dto.ConfigInstruction;
import com.configsystem.master.dto.UpgradePackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UpgradeService {

    @Autowired private ReleaseService releaseService;
    @Autowired private DiffService diffService;

    public UpgradePackage generateUpgradePath(String fromVersion, String toVersion) {
        // FR-4: Reconstruct both states
        Map<String, String> currentState = releaseService.getVersionState(fromVersion);
        Map<String, String> targetState = releaseService.getVersionState(toVersion);

        // FR-6: Calculate the differences
        List<ConfigInstruction> instructions = diffService.calculateInstructions(currentState, targetState);

        return new UpgradePackage(fromVersion, toVersion, instructions);
    }
}