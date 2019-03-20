package com.westernacher.internal.feedback.service;

import com.westernacher.internal.feedback.domain.*;
import com.westernacher.internal.feedback.repository.AppraisalCycleRepository;
import com.westernacher.internal.feedback.repository.AppraisalRepository;
import com.westernacher.internal.feedback.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AppraisalCycleService {

    @Autowired
    private AppraisalCycleRepository repository;

    @Autowired
    private AppraisalRepository appraisalRepository;

    @Autowired
    private PersonRepository personRepository;

    public AppraisalCycle create(AppraisalCycle appraisalCycle) {

        appraisalCycle.setStatus(AppraisalCycleStatusType.OPEN);

        AppraisalCycle cycle = repository.save(appraisalCycle);

        personRepository.findAll().forEach(item -> {
            List<ObjectiveResponseGroup> responseGroupList = new ArrayList<>();
            cycle.getSectiononeCriteria().forEach(criteriaGroup -> responseGroupList.add(generateResponseGroup(criteriaGroup)));
            List<SubjectiveResponse> sectiontwo = new ArrayList<>();
            List<SubjectiveResponse> sectionthree = new ArrayList<>();
            Appraisal appraisal = Appraisal.builder()
                    .cycleId(cycle.getId())
                    .userId(item.getId())
                    .sectiononeResponse(responseGroupList)
                    .sectiontwoResponse(sectiontwo)
                    .sectionthreeResponse(sectionthree)
                    .status(AppraisalStatusType.SELF_REVIEW)
                    .build();
            appraisalRepository.save(appraisal);
        });

        return cycle;
    }

    private ObjectiveResponseGroup generateResponseGroup(CriteriaGroup criteriaGroup) {

        List<ObjectiveResponse> responseList = new ArrayList<>();

        criteriaGroup.getCriterias().forEach(item ->
                responseList.add(ObjectiveResponse.builder()
                        .weightage(item.getWeightage())
                        .criteria(item.getText())
                        .build())
        );

        return ObjectiveResponseGroup.builder()
                .group(criteriaGroup.getGroup())
                .response(responseList)
                .build();

    }

    public void activate(String id) {
        AppraisalCycle cycle = repository.findById(id).orElse(null);
        cycle.setStatus(AppraisalCycleStatusType.ACTIVE.ACTIVE);
        cycle.setStartDate(new Date());
        repository.save(cycle);
    }
}
