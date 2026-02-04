package com.configsystem.master.dto;

// A clean version of the entries (No IDs, just key/value)
public record ConfigEntryDTO(
        String key,
        String value
) {}