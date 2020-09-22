
package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.domain.AppraisalGoal;
import com.westernacher.internal.feedback.domain.AppraisalReview;
import com.westernacher.internal.feedback.domain.AppraisalReviewGoal;
import com.westernacher.internal.feedback.domain.AppraisalStatusType;
import com.westernacher.internal.feedback.repository.AppraisalGoalRepository;
import com.westernacher.internal.feedback.repository.AppraisalReviewGoalRepository;
import com.westernacher.internal.feedback.repository.AppraisalReviewRepository;
import com.westernacher.internal.feedback.service.AppraisalReviewGoalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.westernacher.internal.feedback.domain.AppraisalStatusType.PROJECT_MANAGER;

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
