package com.westernacher.internal.feedback.controller.v2;


import com.westernacher.internal.feedback.domain.v1Goal;
import com.westernacher.internal.feedback.domain.v2.Goal;
import com.westernacher.internal.feedback.repository.v2.GoalRepository;
import com.westernacher.internal.feedback.service.v2.GoalService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/appraisal/custom/goal")
public class GoalController {
    @Autowired
    private GoalRepository repository;

    @Autowired
    private GoalService goalService;

    @GetMapping
    public List<Goal> getAll() {
        return repository.findAll();
    }


    @PostMapping
    public ResponseEntity<List<Goal>> create(@Valid @RequestBody List<Goal> goals) {
        return ResponseEntity.ok(repository.saveAll(goals));
    }

    @GetMapping({"{employeeId}"})
     public List<v1Goal> getGoalsForEmployee(@PathVariable String employeeId){
        return goalService.getGoalsForEmployee(employeeId);
    }

}


