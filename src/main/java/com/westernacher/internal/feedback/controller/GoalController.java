package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.domain.Goal;
import com.westernacher.internal.feedback.repository.GoalRepository;
import com.westernacher.internal.feedback.service.GoalService;
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
@RequestMapping("/goal")
public class GoalController {

    @Autowired
    private GoalRepository repository;

    @Autowired
    private GoalService service;

    @GetMapping
    public ResponseEntity<List<Goal>> getAll () {
        return ResponseEntity.ok(repository.findAll());
    }

    @PostMapping
    public ResponseEntity<?> saveGoalDefinition (@RequestBody List<Goal> goalDefinitions) {
        return ResponseEntity.ok(repository.saveAll(goalDefinitions));
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadGoalFile(@RequestParam MultipartFile file) {
        if (StringUtils.cleanPath(file.getOriginalFilename()).endsWith(".csv")) {
            return ResponseEntity.ok(service.uploadGoalCsvFile(file));
        } else  {
            return new ResponseEntity<>("Wrong file format", HttpStatus.BAD_REQUEST);
        }
    }
}


