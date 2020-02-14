package com.westernacher.internal.feedback.service;

import com.westernacher.internal.feedback.domain.*;
import com.westernacher.internal.feedback.repository.AppraisalCycleRepository;
import com.westernacher.internal.feedback.repository.AppraisalRepository;
import com.westernacher.internal.feedback.repository.GoalDefinitionRepository;
import com.westernacher.internal.feedback.repository.PersonRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class AppraisalCycleService {

    @Autowired
    private AppraisalCycleRepository repository;

    @Autowired
    private GoalDefinitionRepository goalDefinitionRepository;

    @Autowired
    private AppraisalRepository appraisalRepository;

    @Autowired
    private PersonRepository personRepository;

    public AppraisalCycle create(AppraisalCycle appraisalCycle) {

        appraisalCycle.setStatus(AppraisalCycleStatusType.OPEN);

        AppraisalCycle cycle = repository.save(appraisalCycle);

        personRepository.findAll().forEach(item -> {
            List<ObjectiveResponseGroup> sectionone = new ArrayList<>();

            sectionone.addAll(generateResponseGroup(goalDefinitionRepository.getAllByJobName(item.getJobName())));
            List<SubjectiveResponse> sectiontwo = new ArrayList<>();
            List<SubjectiveResponse> sectionthree = new ArrayList<>();
            Appraisal appraisal = Appraisal.builder()
                    .cycleId(cycle.getId())
                    .userId(item.getId())
                    .sectiononeResponse(sectionone)
                    .sectiontwoResponse(sectiontwo)
                    .sectionthreeResponse(sectionthree)
                    .status(AppraisalStatusType.SELF_REVIEW)
                    .build();
            appraisalRepository.save(appraisal);
        });

        return cycle;
    }

    private List<ObjectiveResponseGroup> generateResponseGroup(List<GoalDefinition> goalDefinitionList) {

        List<ObjectiveResponseGroup> responseList = new ArrayList<>();

        Map<String, List<ObjectiveResponse>> map = new HashMap<>();

        goalDefinitionList.forEach(item -> {
            ObjectiveResponse objectiveResponse = ObjectiveResponse
                    .builder()
                    .criteria(item.getCriteria())
                    .weightage(item.getWeightage())
                    .reviews(new HashMap<>())
                    .build();
            if (map.containsKey(item.getGroup())) {
                map.get(item.getGroup()).add(objectiveResponse);
            } else {
                List<ObjectiveResponse> list = new ArrayList<>();
                list.add(objectiveResponse);
                map.put(item.getGroup(), list);
            }
        });

        for (String key : map.keySet()) {
            ObjectiveResponseGroup objectiveResponseGroup = ObjectiveResponseGroup
                    .builder()
                    .group(key)
                    .response(map.get(key))
                    .build();
            responseList.add(objectiveResponseGroup);
        }

        return responseList;

    }

    public void activate(String id) {
        AppraisalCycle cycle = repository.findById(id).orElse(null);
        cycle.setStatus(AppraisalCycleStatusType.ACTIVE.ACTIVE);
        cycle.setStartDate(new Date());
        repository.save(cycle);
    }
}
