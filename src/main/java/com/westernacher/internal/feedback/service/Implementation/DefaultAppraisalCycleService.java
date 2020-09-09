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
    private GoalRepository goalRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AppraisalReviewGoalRepository reviewGoalRepository;

    @Autowired
    private AppraisalReviewRepository appraisalReviewRepository;

    @Autowired
    private AppraisalGoalRepository appraisalGoalRepository;

    @Autowired
    private AppraisalRoleRepository appraisalRoleRepository;

    public AppraisalCycle create(AppraisalCycle appraisalCycle) {

        appraisalCycle.setStatus(AppraisalCycleStatusType.ACTIVE);
        AppraisalCycle cycle = repository.save(appraisalCycle);

        List<Goal> goals = goalRepository.findAll();
        goals.stream().forEach(goal -> {
            AppraisalGoal appraisalGoal = new AppraisalGoal();
            appraisalGoal.setJobName(goal.getJobName());
            appraisalGoal.setGroup(goal.getGroup());
            appraisalGoal.setCriteria(goal.getCriteria());
            appraisalGoal.setWeightage(goal.getWeightage());
            appraisalGoal.setDescription(goal.getDescription());
            appraisalGoal.setCycleId(cycle.getId());
            appraisalGoalRepository.save(appraisalGoal);
        });

        List<Role> roleListt = roleRepository.findAll();
        roleListt.stream().forEach(role -> {
            AppraisalRole appraisalRole = new AppraisalRole();
            appraisalRole.setReviewerId(role.getReviewerId());
            appraisalRole.setRoleType(role.getRoleType());
            appraisalRole.setEmployeeId(role.getEmployeeId());
            appraisalRole.setCycleId(cycle.getId());
            appraisalRoleRepository.save(appraisalRole);

        });

        Map<String, List<AppraisalGoal>> goalDefinitionMap = new HashMap<>();
        List<AppraisalGoal> goalDefinitions = appraisalGoalRepository.findAllByCycleId(cycle.getId());

        for (AppraisalGoal goalDefinition : goalDefinitions) {
            if (goalDefinitionMap.containsKey(goalDefinition.getJobName())) {
                List<AppraisalGoal> goalDefinitionList = goalDefinitionMap.get(goalDefinition.getJobName());
                goalDefinitionList.add(goalDefinition);
                goalDefinitionMap.put(goalDefinition.getJobName(), goalDefinitionList);

            } else {
                List<AppraisalGoal> goalDefinitionList = new ArrayList<>();
                goalDefinitionList.add(goalDefinition);
                goalDefinitionMap.put(goalDefinition.getJobName(), goalDefinitionList);
            }
        }

        Map<String, List<AppraisalRole>> roleMap = new HashMap<>();
        List<AppraisalRole> roles = appraisalRoleRepository.findAllByCycleId(cycle.getId());

        for (AppraisalRole role : roles) {
            if (roleMap.containsKey(role.getEmployeeId())) {
                List<AppraisalRole> roleList = roleMap.get(role.getEmployeeId());
                roleList.add(role);
                roleMap.put(role.getEmployeeId(), roleList);

            } else {
                List<AppraisalRole> roleList = new ArrayList<>();
                roleList.add(role);
                roleMap.put(role.getEmployeeId(), roleList);
            }
        }

        personRepository.findAll().forEach(person -> { //personId : empId

            //get goals for the person depend on jobtype : take local storage
            if (roleMap.containsKey(person.getId())) {
                AppraisalReview appraisalReview = new AppraisalReview();
                appraisalReview.setCycleId(cycle.getId());
                appraisalReview.setEmployeeId(person.getId());
                appraisalReview.setStatus(AppraisalStatusType.SELF_APPRAISAL);
                AppraisalReview savedReview = appraisalReviewRepository.save(appraisalReview);
                goalDefinitionMap.get(person.getJobName()).stream().forEach(goalDefinition -> {
                    AppraisalReviewGoal selfAppraisalReviewGoal = new AppraisalReviewGoal();
                    selfAppraisalReviewGoal.setEmployeeId(person.getId());
                    selfAppraisalReviewGoal.setAppraisalId(savedReview.getId());
                    selfAppraisalReviewGoal.setReviewerId(person.getId());
                    selfAppraisalReviewGoal.setReviewerType(RoleType.Self);  // it is not correct
                    selfAppraisalReviewGoal.setGoalId(goalDefinition.getId());
                    selfAppraisalReviewGoal.setComment("");
                    selfAppraisalReviewGoal.setRating("");
                    selfAppraisalReviewGoal.setComplete(false);
                    reviewGoalRepository.save(selfAppraisalReviewGoal);

                });

                roleMap.get(person.getId()).forEach(role -> {
                    goalDefinitionMap.get(person.getJobName()).stream().forEach(goalDefinition -> {
                        AppraisalReviewGoal appraisalReviewGoal = new AppraisalReviewGoal();
                        appraisalReviewGoal.setEmployeeId(person.getId());
                        appraisalReviewGoal.setAppraisalId(savedReview.getId());
                        appraisalReviewGoal.setReviewerId(role.getReviewerId());
                        appraisalReviewGoal.setReviewerType(role.getRoleType());
                        appraisalReviewGoal.setGoalId(goalDefinition.getId());
                        appraisalReviewGoal.setComment("");
                        appraisalReviewGoal.setRating("");
                        appraisalReviewGoal.setComplete(false);
                        reviewGoalRepository.save(appraisalReviewGoal);
                    });
                });
            }
        });



        return cycle;
    }
}
