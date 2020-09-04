
package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.domain.AppraisalReview;
import com.westernacher.internal.feedback.domain.AppraisalReviewGoal;
import com.westernacher.internal.feedback.repository.AppraisalReviewGoalRepository;
import com.westernacher.internal.feedback.repository.AppraisalReviewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/appraisal/review")
@Slf4j
public class AppraisalReviewController {

    @Autowired
    private AppraisalReviewRepository repository;

    @Autowired
    private AppraisalReviewGoalRepository reviewGoalRepository;

    @GetMapping
    public ResponseEntity<?> getAllReview () {
        return ResponseEntity.ok(repository.findAll());
    }

    @PostMapping("/submit")
    public void submit(@RequestParam String appraisalId,
                                  @RequestParam String reviewerId) {

        List<AppraisalReviewGoal> reviewGoals = reviewGoalRepository.findAllByAppraisalIdAndReviewerId(appraisalId, reviewerId);
        reviewGoals.stream().forEach(appraisalReviewGoal -> {
            appraisalReviewGoal.setComplete(true);
            reviewGoalRepository.save(appraisalReviewGoal);
        });
    }
}
