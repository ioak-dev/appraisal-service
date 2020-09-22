package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.controller.representation.MigrationAppraisalPayload;
import com.westernacher.internal.feedback.domain.MigrationOutput;
import com.westernacher.internal.feedback.service.MigrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/migrate")
public class MigrationController {

    @Autowired
    private MigrationService service;

    @PostMapping
    public ResponseEntity<?> migrateAppraisal (@RequestParam String cycleId,
                                               @RequestBody MigrationAppraisalPayload payload) {
        return ResponseEntity.ok(service.migrate(cycleId, payload.getAppraisals(), service.getPersonMap(payload.getPersons()), payload.getGoalOrder()));
    }

    @PostMapping("/new")
    public void createNewAppraisalData(@RequestBody MigrationOutput output) {
        service.migrateToNewDb(output);
    }
}


