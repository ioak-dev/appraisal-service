package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.controller.representation.FeedbackCycleResource;
import com.westernacher.internal.feedback.domain.FeedbackCycle;
import com.westernacher.internal.feedback.repository.FeedbackCycleRepository;
import com.westernacher.internal.feedback.service.FeedbackCycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

public class FeedbackCycleController {

    @Autowired
    private FeedbackCycleRepository repository;
    @Autowired
    private FeedbackCycleService service;

    @RequestMapping(method = RequestMethod.GET)
    public List<FeedbackCycle> getAll () {
        return repository.findAll();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public FeedbackCycle get (@PathVariable("id") String id) {
        return repository.findById(id).orElse(null);
    }

    @RequestMapping(value = "/unit/{unit}", method = RequestMethod.GET)
    public List<FeedbackCycle> getByUnit (@PathVariable("unit") String unit) {
        return repository.findAllByUnit(unit);
    }

    @RequestMapping(method = RequestMethod.POST)
    public FeedbackCycle create (@Valid @RequestBody FeedbackCycle feedbackCycle) {
        return service.create(feedbackCycle);
    }

    @RequestMapping(value = "/{id}/activate", method = RequestMethod.POST)
    public void activate (@PathVariable("id") String id) {
        service.activate(id);
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete (@PathVariable("id") String id) {
        repository.deleteById(id);
    }

    @RequestMapping(value = "/{id}/extend/{days}", method = RequestMethod.POST)
    public FeedbackCycle extend (@PathVariable("id") String id, @PathVariable("days") int days) {
        return service.extend(id, days);
    }

    @RequestMapping(value = "/{id}/template", method = RequestMethod.PUT)
    public FeedbackCycle updateQuestions (@PathVariable("id") String cycleId, @Valid @RequestBody FeedbackCycleResource.QuestionResource resource) {
         return service.updateQuestions(cycleId, resource.getQuestions());
    }

}


