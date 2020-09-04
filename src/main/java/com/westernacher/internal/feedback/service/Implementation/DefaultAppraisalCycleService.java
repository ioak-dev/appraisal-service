package com.westernacher.internal.feedback.service.Implementation;

import com.westernacher.internal.feedback.domain.*;
import com.westernacher.internal.feedback.repository.*;
import com.westernacher.internal.feedback.service.AppraisalCycleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class DefaultAppraisalCycleService implements AppraisalCycleService {

    @Autowired
    private AppraisalCycleRepository repository;

    @Autowired
    private GoalDefinitionRepository goalDefinitionRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AppraisalReviewGoalRepository reviewGoalRepository;

    public AppraisalCycle create(AppraisalCycle appraisalCycle) {



        // map : empId : List of Roles
        //map jobname : list of goal defination

        Map<String, List<GoalDefinition>> goalDefinitionMap = new HashMap<>();
        List<GoalDefinition> goalDefinitions = goalDefinitionRepository.findAll();

        for (GoalDefinition goalDefinition : goalDefinitions) {
            if (goalDefinitionMap.containsKey(goalDefinition.getJobName())) {
                List<GoalDefinition> goalDefinitionList = goalDefinitionMap.get(goalDefinition.getJobName());
                goalDefinitionList.add(goalDefinition);
                goalDefinitionMap.put(goalDefinition.getJobName(), goalDefinitionList);

            } else {
                List<GoalDefinition> goalDefinitionList = new ArrayList<>();
                goalDefinitionList.add(goalDefinition);
                goalDefinitionMap.put(goalDefinition.getJobName(), goalDefinitionList);
            }
        }

        Map<String, List<Role>> roleMap = new HashMap<>();
        List<Role> roles = roleRepository.findAll();

        for (Role role : roles) {
            if (roleMap.containsKey(role.getEmployeeId())) {
                List<Role> roleList = roleMap.get(role.getEmployeeId());
                roleList.add(role);
                roleMap.put(role.getEmployeeId(), roleList);

            } else {
                List<Role> roleList = new ArrayList<>();
                roleList.add(role);
                roleMap.put(role.getEmployeeId(), roleList);
            }
        }

        personRepository.findAll().forEach(person -> { //personId : empId
            //get goals for the person depend on jobtype : take local storage
            if (roleMap.containsKey(person.getId())) {
                goalDefinitionMap.get(person.getJobName()).stream().forEach(goalDefinition -> {
                    AppraisalReviewGoal selfAppraisalReviewGoal = new AppraisalReviewGoal();
                    selfAppraisalReviewGoal.setEmployeeId(person.getId());
                    selfAppraisalReviewGoal.setReviewerId(person.getId());
                    selfAppraisalReviewGoal.setReviewerType(RoleType.Self);  // it is not correct
                    selfAppraisalReviewGoal.setGoalDefinitionId(goalDefinition.getId());
                    selfAppraisalReviewGoal.setComment("");
                    selfAppraisalReviewGoal.setRating("");
                    reviewGoalRepository.save(selfAppraisalReviewGoal);

                });

                roleMap.get(person.getId()).forEach(role -> {
                    goalDefinitionMap.get(person.getJobName()).stream().forEach(goalDefinition -> {
                        AppraisalReviewGoal appraisalReviewGoal = new AppraisalReviewGoal();
                        appraisalReviewGoal.setEmployeeId(person.getId());
                        appraisalReviewGoal.setReviewerId(role.getReviewerId());
                        appraisalReviewGoal.setReviewerType(role.getRoleType());  // it is not correct
                        appraisalReviewGoal.setGoalDefinitionId(goalDefinition.getId());
                        appraisalReviewGoal.setComment("");
                        appraisalReviewGoal.setRating("");
                        reviewGoalRepository.save(appraisalReviewGoal);
                    });
                });
            }
        });

        appraisalCycle.setStatus(AppraisalCycleStatusType.ACTIVE);
        AppraisalCycle cycle = repository.save(appraisalCycle);

        return cycle;
    }
}
