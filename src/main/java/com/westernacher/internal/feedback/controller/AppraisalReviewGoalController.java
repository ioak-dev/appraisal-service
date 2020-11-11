
package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.domain.AppraisalReviewGoal;
import com.westernacher.internal.feedback.domain.AppraisalRole;
import com.westernacher.internal.feedback.repository.AppraisalRoleRepository;
import com.westernacher.internal.feedback.service.AppraisalReviewGoalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/appraisal/review/goal")
@Slf4j
public class AppraisalReviewGoalController {

    @Autowired
    private AppraisalReviewGoalService service;

    @Autowired
    private AppraisalRoleRepository approsalRoleRepository;

    @GetMapping
    public ResponseEntity<?> getReviewGoals (@RequestParam String appraisalId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();

        /*if ((name != null && !name.isEmpty()) &&
                (!approsalRoleRepository.findAllByEmployeeId(name).isEmpty() || !approsalRoleRepository.findAllByReviewerId(name).isEmpty())) {
            return ResponseEntity.ok(service.getReviewGoals(appraisalId));
        }
        return ResponseEntity.noContent().build();*/
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
