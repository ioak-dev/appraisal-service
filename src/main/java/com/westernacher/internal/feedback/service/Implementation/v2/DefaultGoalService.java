package com.westernacher.internal.feedback.service.Implementation.v2;


import com.westernacher.internal.feedback.domain.v1Goal;
import com.westernacher.internal.feedback.domain.v2.Person;
import com.westernacher.internal.feedback.repository.v1GoalRepository;
import com.westernacher.internal.feedback.repository.v2.PersonRepository;
import com.westernacher.internal.feedback.service.v2.GoalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DefaultGoalService implements GoalService {

    @Autowired
    private v1GoalRepository repository;

    @Autowired
    private PersonRepository personRepository;

    public List<v1Goal> getGoalsForEmployee(String employeeId) {
        String jobCode;
        Optional<Person> person = personRepository.findById(employeeId);
        if (person.isPresent()) {
            jobCode = person.get().getJob();
            List<v1Goal> goalList = repository.findAllByJob(jobCode);
            return goalList;
        } else
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
