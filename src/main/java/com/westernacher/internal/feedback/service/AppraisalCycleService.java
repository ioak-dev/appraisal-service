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

        Map<String, List<String>> projectManagerRoleMap = new HashMap<>();
        Map<String, List<String>> teamLeadRoleMap = new HashMap<>();
        Map<String, List<String>> practiceDirectorRoleMap = new HashMap<>();
        Map<String, List<String>> administratorRoleMap = new HashMap<>();

        personRepository.findAll().forEach(person -> {
            person.getRoles().forEach(role -> {
                if (role.getType().equals(RoleType.ProjectManager)) {
                    role.getOptions().forEach(email -> {
                        if (projectManagerRoleMap.containsKey(email)) {
                            projectManagerRoleMap.get(email).add(person.getId());
                        } else {
                            List<String> list = new ArrayList<>();
                            list.add(person.getId());
                            projectManagerRoleMap.put(email, list);
                        }
                    });
                } else if (role.getType().equals(RoleType.TeamLead)) {
                    role.getOptions().forEach(email -> {
                        if (teamLeadRoleMap.containsKey(email)) {
                            teamLeadRoleMap.get(email).add(person.getId());
                        } else {
                            List<String> list = new ArrayList<>();
                            list.add(person.getId());
                            teamLeadRoleMap.put(email, list);
                        }
                    });
                } else if (role.getType().equals(RoleType.PracticeDirector)) {
                    role.getOptions().forEach(email -> {
                        if (practiceDirectorRoleMap.containsKey(email)) {
                            practiceDirectorRoleMap.get(email).add(person.getId());
                        } else {
                            List<String> list = new ArrayList<>();
                            list.add(person.getId());
                            practiceDirectorRoleMap.put(email, list);
                        }
                    });
                } else if (role.getType().equals(RoleType.Administrator)) {
                    role.getOptions().forEach(email -> {
                        if (administratorRoleMap.containsKey(email)) {
                            administratorRoleMap.get(email).add(person.getId());
                        } else {
                            List<String> list = new ArrayList<>();
                            list.add(person.getId());
                            administratorRoleMap.put(email, list);
                        }
                    });
                }
            });
        });

        personRepository.findAll().forEach(person -> {
            List<ObjectiveResponseGroup> sectionone = new ArrayList<>();

            sectionone.addAll(generateResponseGroup(projectManagerRoleMap, teamLeadRoleMap, practiceDirectorRoleMap, administratorRoleMap, person, goalDefinitionRepository.getAllByJobName(person.getJobName())));
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

    private List<ObjectiveResponseGroup> generateResponseGroup(Map<String, List<String>> projectManagerRoleMap,
                                                               Map<String, List<String>> teamLeadRoleMap,
                                                               Map<String, List<String>> practiceDirectorRoleMap,
                                                               Map<String, List<String>> administratorRoleMap,
                                                               Person person, List<GoalDefinition> goalDefinitionList) {

        List<ObjectiveResponseGroup> responseList = new ArrayList<>();

        Map<String, List<ObjectiveResponse>> map = new HashMap<>();

        goalDefinitionList.forEach(item -> {
            ObjectiveResponse objectiveResponse = ObjectiveResponse
                    .builder()
                    .criteria(item.getCriteria())
                    .description(item.getDescription())
                    .weightage(item.getWeightage())
                    .projectManagerReviews(getReviewerElements(projectManagerRoleMap, person))
                    .teamLeadReviews(getReviewerElements(teamLeadRoleMap, person))
                    .practiceDirectorReviews(getReviewerElements(practiceDirectorRoleMap, person))
                    .administratorReviews(getReviewerElements(administratorRoleMap, person))
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

    private Map<String, ReviewerElements> getReviewerElements(Map<String, List<String>> roleMap, Person person) {
        Map<String, ReviewerElements> map = new HashMap<>();

        if (roleMap.get(person.getEmail()) != null) {
            roleMap.get(person.getEmail()).forEach(managerId -> {
                Person reviewer = personRepository.findPersonById(managerId);
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
        return map;
    }

    public void activate(String id) {
        AppraisalCycle cycle = repository.findById(id).orElse(null);
        cycle.setStatus(AppraisalCycleStatusType.ACTIVE.ACTIVE);
        cycle.setStartDate(new Date());
        repository.save(cycle);
    }
}
