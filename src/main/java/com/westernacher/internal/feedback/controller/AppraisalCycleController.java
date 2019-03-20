package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.domain.AppraisalCycle;
import com.westernacher.internal.feedback.repository.AppraisalCycleRepository;
import com.westernacher.internal.feedback.service.AppraisalCycleService;
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

    @RequestMapping(method = RequestMethod.GET)
    public List<AppraisalCycle> getAll () {
        return repository.findAll();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public AppraisalCycle get (@PathVariable("id") String id) {
        return repository.findById(id).orElse(null);
    }

    @RequestMapping(method = RequestMethod.POST)
    public AppraisalCycle create (@Valid @RequestBody AppraisalCycle appraisalCycle) {
        return service.create(appraisalCycle);
    }

    @RequestMapping(value = "/{id}/activate", method = RequestMethod.POST)
    public void activate (@PathVariable("id") String id) {
        service.activate(id);
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete (@PathVariable("id") String id) {
        repository.deleteById(id);
    }

}


