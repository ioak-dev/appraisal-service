package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.domain.GoalDefinition;
import com.westernacher.internal.feedback.domain.RatingScale;
import com.westernacher.internal.feedback.repository.GoalDefinitionRepository;
import com.westernacher.internal.feedback.repository.RatingScaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ratingScale")
public class RatingScaleController {

    @Autowired
    private RatingScaleRepository repository;

    @RequestMapping(method = RequestMethod.GET)
    public List<RatingScale> getAll () {
        return repository.findAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    public void saveRatingScale (@RequestBody List<RatingScale> ratingScales) {
        repository.saveAll(ratingScales);
    }
}


