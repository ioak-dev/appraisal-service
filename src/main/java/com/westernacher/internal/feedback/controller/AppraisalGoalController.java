
package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.repository.AppraisalGoalRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appraisal/goal")
@Slf4j
public class AppraisalGoalController {

    @Autowired
    private AppraisalGoalRepository repository;

    @GetMapping
    public ResponseEntity<?> getAppraisalGoals (@RequestParam String cycleId) {
        return ResponseEntity.ok(repository.findAllByCycleId(cycleId));
    }
}
