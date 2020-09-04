package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.domain.AppraisalCycle;
import com.westernacher.internal.feedback.repository.AppraisalCycleRepository;
import com.westernacher.internal.feedback.service.AppraisalCycleService;
import com.westernacher.internal.feedback.service.Implementation.DefaultAppraisalCycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/cycle")
public class AppraisalCycleController {

    @Autowired
    private AppraisalCycleRepository repository;
    @Autowired
    private AppraisalCycleService service;

    @GetMapping
    public List<AppraisalCycle> getAll () {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public AppraisalCycle get (@PathVariable("id") String id) {
        return repository.findById(id).orElse(null);
    }

    @PostMapping
    public AppraisalCycle create (@Valid @RequestBody AppraisalCycle appraisalCycle) {
        return service.create(appraisalCycle);
    }

    @DeleteMapping("/{id}")
    public void delete (@PathVariable("id") String id) {
        repository.deleteById(id);
    }

}


