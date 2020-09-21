package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.controller.representation.MigrationAppraisalPayload;
import com.westernacher.internal.feedback.domain.Appraisal;
import com.westernacher.internal.feedback.domain.Goal;
import com.westernacher.internal.feedback.repository.GoalRepository;
import com.westernacher.internal.feedback.service.GoalService;
import com.westernacher.internal.feedback.service.Implementation.MigrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/migrate")
public class MigrationController {

    @Autowired
    private MigrationService service;

    @PostMapping
    public ResponseEntity<?> migrateAppraisal (@RequestParam String cycleId,
                                               @RequestBody MigrationAppraisalPayload payload) {
        // payload.getPersons() => personMap
        return ResponseEntity.ok(service.migrate(payload.getAppraisals(), service.getPersonMap(payload.getPersons()), payload.getGoalOrder()));
    }
}


