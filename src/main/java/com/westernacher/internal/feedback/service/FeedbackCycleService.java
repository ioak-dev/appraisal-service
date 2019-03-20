package com.westernacher.internal.feedback.service;

import com.westernacher.internal.feedback.domain.FeedbackCycle;
import com.westernacher.internal.feedback.domain.FeedbackCycleStatusType;
import com.westernacher.internal.feedback.domain.Template;
import com.westernacher.internal.feedback.repository.FeedbackCycleRepository;
import com.westernacher.internal.feedback.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class FeedbackCycleService {

    @Autowired
    private FeedbackCycleRepository repository;

    @Autowired
    private TemplateRepository templateRepository;


    public FeedbackCycle updateQuestions(String cycleId, List<String> questions) {
        FeedbackCycle cycle = repository.findById(cycleId).orElse(null);

        cycle.setQuestions(questions);

        return repository.save(cycle);
    }

    public FeedbackCycle extend(String cycleId, int days) {
        FeedbackCycle cycle = repository.findById(cycleId).orElse(null);

        cycle.setEndDate(new Date(cycle.getEndDate().getTime() + (1000 * 60 * 60 * 24 * days)));

        repository.save(cycle);

        return cycle;
    }

    public FeedbackCycle create(FeedbackCycle feedbackCycle) {

        feedbackCycle.setName(getFeedbackCycleName(feedbackCycle));

        Template template = templateRepository.findFirstByUnit(feedbackCycle.getUnit());
        if (template != null) {
            feedbackCycle.setQuestions(template.getQuestions());
        }
        feedbackCycle.setStatus(FeedbackCycleStatusType.OPEN);
        return repository.save(feedbackCycle);
    }

    private String getFeedbackCycleName(FeedbackCycle feedbackCycle) {

        if (feedbackCycle.getName() == null || feedbackCycle.getName().isEmpty()) {
            Calendar today = Calendar.getInstance();
            today.setTime(new Date());

            int year = today.get(Calendar.YEAR);
            String quarter = "";

            if (today.get(Calendar.MONTH) <= 3) {
                year = year - 1;
                quarter = "Q4";
            } else if (today.get(Calendar.MONTH) <= 6) {
                quarter = "Q1";
            } else if (today.get(Calendar.MONTH) <= 9) {
                quarter = "Q2";
            } else {
                quarter = "Q3";
            }

            return feedbackCycle.getUnit() + " " + year + " " + quarter;

        } else {
            return feedbackCycle.getName();
        }

    }

    public void activate(String id) {
        FeedbackCycle cycle = repository.findById(id).orElse(null);
        cycle.setStatus(FeedbackCycleStatusType.ACTIVE);
        cycle.setStartDate(new Date());
        repository.save(cycle);
    }
}
