
package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.domain.AppraisalReviewGoal;
import com.westernacher.internal.feedback.service.AppraisalReviewGoalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/appraisal/review/goal")
@Slf4j
public class AppraisalReviewGoalController {

    @Autowired
    private AppraisalReviewGoalService service;

    @GetMapping
    public ResponseEntity<?> getReviewGoals (@RequestParam String appraisalId) {
        return ResponseEntity.ok(service.getReviewGoals(appraisalId));
    }

    @PutMapping
    public ResponseEntity<?> update (@RequestBody List<AppraisalReviewGoal> reviewGoals) {
        return ResponseEntity.ok(service.update(reviewGoals));
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submit (@RequestBody List<AppraisalReviewGoal> reviewGoals) {
        return ResponseEntity.ok(service.submit(reviewGoals));
    }
}
