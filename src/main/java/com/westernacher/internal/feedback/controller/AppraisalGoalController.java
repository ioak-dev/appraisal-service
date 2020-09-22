
package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.domain.AppraisalGoal;
import com.westernacher.internal.feedback.repository.AppraisalGoalRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/appraisal/goal")
@Slf4j
public class AppraisalGoalController {

    @Autowired
    private AppraisalGoalRepository repository;

    @GetMapping
    public ResponseEntity<?> getAppraisalGoals (@RequestParam String cycleId) {
        List<AppraisalGoal> appraisalGoals = repository.findAllByCycleId(cycleId);
        appraisalGoals.sort(
                Comparator.comparing((AppraisalGoal ag) -> ag.getOrder()));
        return ResponseEntity.ok(appraisalGoals);
    }
}
