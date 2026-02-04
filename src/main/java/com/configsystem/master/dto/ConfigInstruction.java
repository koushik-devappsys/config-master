package com.configsystem.master.dto;

public record ConfigInstruction (
    String op,      // "ADD", "UPDATE", or "DELETE"
    String key,
    String value
){}
