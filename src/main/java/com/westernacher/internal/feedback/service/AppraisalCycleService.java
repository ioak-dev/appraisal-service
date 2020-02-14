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

        personRepository.findAll().forEach(person -> {
            List<ObjectiveResponseGroup> sectionone = new ArrayList<>();

            sectionone.addAll(generateResponseGroup(person, goalDefinitionRepository.getAllByJobName(person.getJobName())));
            List<SubjectiveResponse> sectiontwo = new ArrayList<>();
            List<SubjectiveResponse> sectionthree = new ArrayList<>();
            Appraisal appraisal = Appraisal.builder()
                    .cycleId(cycle.getId())
                    .userId(person.getId())
                    .sectiononeResponse(sectionone)
                    .sectiontwoResponse(sectiontwo)
                    .sectionthreeResponse(sectionthree)
                    .status(AppraisalStatusType.SELF_REVIEW)
                    .build();
            appraisalRepository.save(appraisal);
        });

        return cycle;
    }

    private List<ObjectiveResponseGroup> generateResponseGroup(Person person, List<GoalDefinition> goalDefinitionList) {

        List<ObjectiveResponseGroup> responseList = new ArrayList<>();

        Map<String, List<ObjectiveResponse>> map = new HashMap<>();

        goalDefinitionList.forEach(item -> {
            ObjectiveResponse objectiveResponse = ObjectiveResponse
                    .builder()
                    .criteria(item.getCriteria())
                    .weightage(item.getWeightage())
                    .projectManagerReviews(getReviewerElements(person, RoleType.ProjectManager))
                    .teamLeadReviews(getReviewerElements(person, RoleType.TeamLead))
                    .practiceDirectorReviews(getReviewerElements(person, RoleType.PracticeDirector))
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

    private Map<String, ReviewerElements> getReviewerElements(Person person, RoleType roleType) {
        Map<String, ReviewerElements> map = new HashMap<>();
        person.getRoles().forEach(role -> {
            if (role.getType().equals(roleType)) {
                role.getOptions().forEach(email -> {
                    Person reviewer = personRepository.findPersonByEmail(email);
                    ReviewerElements reviewerElements = ReviewerElements
                            .builder()
                            .comment("")
                            .name(reviewer.getName())
                            .rating("")
                            .isComplete(false)
                            .build();
                    map.put(reviewer.getId(), reviewerElements);
                });
            }
        });
        return map;
    }

    public void activate(String id) {
        AppraisalCycle cycle = repository.findById(id).orElse(null);
        cycle.setStatus(AppraisalCycleStatusType.ACTIVE.ACTIVE);
        cycle.setStartDate(new Date());
        repository.save(cycle);
    }
}
