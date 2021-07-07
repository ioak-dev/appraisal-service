package com.westernacher.internal.feedback.controller.v2;


import com.westernacher.internal.feedback.domain.v2.Goal;
import com.westernacher.internal.feedback.domain.v2.GoalEmployee;
import com.westernacher.internal.feedback.domain.v2.GoalReference;
import com.westernacher.internal.feedback.repository.v2.GoalEmployeeRepository;
import com.westernacher.internal.feedback.repository.v2.GoalReferenceRepository;
import com.westernacher.internal.feedback.repository.v2.GoalRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/appraisal/custom/target")
public class GoalEmployeeController {
    @Autowired
    private GoalEmployeeRepository repository;

    @GetMapping("{employeeId}")
    public List<GoalEmployee> getAll(@PathVariable String employeeId) {
        return repository.findAllByEmployeeId(employeeId);
    }

    @PostMapping("{employeeId}")
    public ResponseEntity<List<GoalEmployee>> create(@RequestBody List<GoalEmployee> targets,
                                                     @PathVariable String employeeId) {
        List<GoalEmployee> targetList = new ArrayList<>();
        for (GoalEmployee target : targets) {
            if (target.getDescription() != null && !target.getDescription().isEmpty()){
                target.setEmployeeId(employeeId);
                targetList.add(target);
            }
        }
        return ResponseEntity.ok(repository.saveAll(targetList));
    }

}


