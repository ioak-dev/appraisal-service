package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.domain.Goal;
import com.westernacher.internal.feedback.repository.GoalRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/goalDefinition")
public class GoalController {

    @Autowired
    private GoalRepository repository;

    @GetMapping
    public List<Goal> getAll () {
        return repository.findAll();
    }



    @RequestMapping(method = RequestMethod.POST)
    public void saveGoalDefinition (@RequestBody List<Goal> goalDefinitions) {
        repository.saveAll(goalDefinitions);
    }
}


