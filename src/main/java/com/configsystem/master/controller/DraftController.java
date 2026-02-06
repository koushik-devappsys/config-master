package com.configsystem.master.controller;

import com.configsystem.master.entity.DraftConfig;
import com.configsystem.master.repository.DraftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    @DeleteMapping("/{regionId}/{key}")
    public ResponseEntity<Void> deleteDraft(@PathVariable Integer regionId, @PathVariable String key) {
        draftRepo.deleteByKeyAndRegion(key, regionId);
        return ResponseEntity.noContent().build();
    }

    @Transactional
    @DeleteMapping("/{regionId}/clear")
    public ResponseEntity<Void> clearAllDrafts(@PathVariable Integer regionId) {
        draftRepo.deleteByRegionId(regionId);
        return ResponseEntity.noContent().build();
    }
}