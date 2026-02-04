package com.configsystem.master.controller;

import com.configsystem.master.entity.DraftConfig;
import com.configsystem.master.repository.DraftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drafts")
public class DraftController {

    @Autowired
    private DraftRepository draftRepo;

    // FR-1: Create/Update configuration records
    @PostMapping
    public DraftConfig saveDraft(@RequestBody DraftConfig draft) {
        return draftRepo.save(draft);
    }

    @GetMapping
    public List<DraftConfig> getAllDrafts() {
        return draftRepo.findAll();
    }
}