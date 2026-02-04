package com.configsystem.master.dto;

import java.util.List;

// A clean version of the Release to show the user
public record ReleaseResponse(
        Long id,
        String versionName,
        Integer regionId,
        String status,
        String publisher,
        List<ConfigEntryDTO> entries
) {}

