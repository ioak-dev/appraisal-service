package com.westernacher.internal.feedback.controller.v1;


import com.westernacher.internal.feedback.domain.v1.AppraisalSubjective;
import com.westernacher.internal.feedback.domain.v1.GoalNew;
import com.westernacher.internal.feedback.repository.v1.AppraisalSubjectiveRepositoryNew;
import com.westernacher.internal.feedback.repository.v1.GoalRepositoryNew;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/goal")
public class GoalControllerNew {
    @Autowired
    private GoalRepositoryNew repository;

    @GetMapping
    public List<GoalNew> getAll () {
        return repository.findAll();
    }


    @PostMapping
    public ResponseEntity<GoalNew> create (@Valid @RequestBody GoalNew goalNew) {
        return ResponseEntity.ok(repository.save(goalNew));
    }

}


