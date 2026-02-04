package com.configsystem.master.dto;

import java.util.List;

public record UpgradePackage(
    String fromVersion,
    String toVersion,
    List<ConfigInstruction> instructions
) {}
