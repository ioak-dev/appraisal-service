
package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.repository.AppraisalGoalRepository;
import com.westernacher.internal.feedback.repository.AppraisalRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appraisal/role")
@Slf4j
public class AppraisalRoleController {

    @Autowired
    private AppraisalRoleRepository repository;

    @GetMapping
    public ResponseEntity<?> getAppraisalRoles (@RequestParam String cycleId) {
        return ResponseEntity.ok(repository.findAllByCycleId(cycleId));
    }
}
