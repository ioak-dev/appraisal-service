package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.domain.GoalDefinition;
import com.westernacher.internal.feedback.domain.Person;
import com.westernacher.internal.feedback.repository.GoalDefinitionRepository;
import com.westernacher.internal.feedback.repository.PersonRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/goalDefinition")
public class GoalDefinitionController {

    @Autowired
    private GoalDefinitionRepository repository;

    @Autowired
    private PersonRepository personRepository;

    @RequestMapping(value="/{userId}", method = RequestMethod.GET)
    public List<GoalDefinition> getAll (@PathVariable("userId") String userId) {
        Person person = personRepository.findPersonById(userId);
        return repository.getAllByJobName(person.getJobName());
    }

    @RequestMapping(method = RequestMethod.POST)
    public void saveGoalDefinition (@RequestBody List<GoalDefinition> goalDefinitions) {
        repository.saveAll(goalDefinitions);
    }
}


