package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.domain.GoalDefinition;
import com.westernacher.internal.feedback.repository.GoalDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/goalDefinition")
public class GoalDefinitionController {

    @Autowired
    private GoalDefinitionRepository repository;

    @RequestMapping(method = RequestMethod.GET)
    public List<GoalDefinition> getAll () {
        return repository.findAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    public void saveGoalDefination (@RequestBody List<GoalDefinition> goalDefinitions) {
        repository.saveAll(goalDefinitions);
    }
}


