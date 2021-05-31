package com.westernacher.internal.feedback.controller.v2;


import com.westernacher.internal.feedback.domain.v2.Goal;
import com.westernacher.internal.feedback.repository.v2.GoalRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/goal")
public class GoalEmployeeController {
    @Autowired
    private GoalRepository repository;

    @GetMapping
    public List<Goal> getAll () {
        return repository.findAll();
    }


    @PostMapping
    public ResponseEntity<Goal> create (@Valid @RequestBody Goal goal) {
        return ResponseEntity.ok(repository.save(goal));
    }

}


