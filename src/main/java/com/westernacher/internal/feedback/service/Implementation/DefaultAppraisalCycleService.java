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

    @Autowired
    private AppraisalReviewMasterRepository appraisalReviewMasterRepository;

    public AppraisalCycle create(AppraisalCycle appraisalCycle) {

        appraisalCycle.setStatus(AppraisalCycleStatusType.ACTIVE);
        AppraisalCycle cycle = repository.save(appraisalCycle);

        /*Persist appraisal goal from goal*/
        appraisalGoalRepository.saveAll(getAppraisalGoalFromGoal(goalRepository.findAll(), cycle.getId()));

        List<Person> personList = personRepository.findAllByCu(cycle.getCu());

        List<String> personIdList = new ArrayList<>();

        personList.stream().forEach(person -> {
            personIdList.add(person.getId());
        });

        /*persist appraisal role from role*/
        appraisalRoleRepository.saveAll(getAppraisalRoleFromRole(roleRepository.findAllByEmployeeIdIn(personIdList), cycle.getId()));

        List<AppraisalGoal> appraisalGoals = appraisalGoalRepository.findAllByCycleId(cycle.getId());

        /*Map of jobs with respective goals*/
        Map<String, List<AppraisalGoal>> goalMap = getJobAndGoalMap(appraisalGoals);

        /*Map of country unit and their respective goals*/
        Map<String, List<AppraisalGoal>> countryUnitMap = getCountryUnitAndGoalMap(appraisalGoals);

        /*Map of only employees and their respective roles*/
        Map<String, List<AppraisalRole>> roleMap = getEmployeeAndAppraisalRoleMap(appraisalRoleRepository.findAllByCycleId(cycle.getId()));

        /*Create appraisal review goal for person's role and job*/
        personList.forEach(person -> {
            if (roleMap.containsKey(person.getId())) {
                createAppraisalreviewGoal(roleMap, goalMap, countryUnitMap, person, cycle.getId());
            }
        });

        return cycle;
    }

    private List<AppraisalGoal> getAppraisalGoalFromGoal(List<Goal> goals, String cycleId) {
        List<AppraisalGoal> appraisalGoalList = new ArrayList<>();
        goals.stream().forEach(goal -> {
            AppraisalGoal appraisalGoal = new AppraisalGoal();
            appraisalGoal.setJob(goal.getJob());
            appraisalGoal.setGroup(goal.getGroup());
            appraisalGoal.setCriteria(goal.getCriteria());
            appraisalGoal.setWeightage(goal.getWeightage());
            appraisalGoal.setDescription(goal.getDescription());
            appraisalGoal.setCycleId(cycleId);
            appraisalGoal.setOrder(goal.getOrder());
            appraisalGoal.setCu(goal.getCu());
            appraisalGoalList.add(appraisalGoal);
        });
        return appraisalGoalList;
    }

    private List<AppraisalRole> getAppraisalRoleFromRole(List<Role> roles, String cycleId) {
        List<AppraisalRole> appraisalRoleList = new ArrayList<>();
        Set<String> employeeList = new HashSet<>();
        roles.stream().forEach(role -> {
            AppraisalRole appraisalRole = new AppraisalRole();
            appraisalRole.setReviewerId(role.getReviewerId());
            appraisalRole.setReviewerType(role.getReviewerType());
            appraisalRole.setEmployeeId(role.getEmployeeId());
            appraisalRole.setCycleId(cycleId);
            appraisalRole.setComplete(false);
            appraisalRole.setTotalScore(0.0d);
            employeeList.add(role.getEmployeeId());
            appraisalRoleList.add(appraisalRole);
        });
        employeeList.stream().forEach(employee-> {
            AppraisalRole appraisalRole = new AppraisalRole();
            appraisalRole.setReviewerId(employee);
            appraisalRole.setReviewerType(AppraisalStatusType.Self);
            appraisalRole.setEmployeeId(employee);
            appraisalRole.setCycleId(cycleId);
            appraisalRole.setComplete(false);
            appraisalRole.setTotalScore(0.0d);
            appraisalRoleList.add(appraisalRole);
        });

        return appraisalRoleList;
    }

    private Map<String, List<AppraisalRole>> getEmployeeAndAppraisalRoleMap(List<AppraisalRole> roles) {
        Map<String, List<AppraisalRole>> roleMap = new HashMap<>();

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
        return roleMap;
    }

    private void createAppraisalreviewGoal(Map<String, List<AppraisalRole>> roleMap, Map<String, List<AppraisalGoal>> goalDefinitionMap,
                                           Map<String, List<AppraisalGoal>> countryUnitMap, Person person, String cycleId) {

        AppraisalReview appraisalReview = new AppraisalReview();
        appraisalReview.setCycleId(cycleId);
        appraisalReview.setEmployeeId(person.getId());
        appraisalReview.setStatus(AppraisalStatusType.Self);
        AppraisalReview savedReview = appraisalReviewRepository.save(appraisalReview);

        List<AppraisalReviewGoal> appraisalReviewGoalList = new ArrayList<>();
        List<AppraisalReviewMaster> appraisalReviewMasters = new ArrayList<>();

        roleMap.get(person.getId()).forEach(role -> {
           if (role.getReviewerType() != AppraisalStatusType.Master) {
               goalDefinitionMap.get(person.getJob()).stream().forEach(goalDefinition -> {
                   AppraisalReviewGoal appraisalReviewGoal = new AppraisalReviewGoal();
                   appraisalReviewGoal.setEmployeeId(person.getId());
                   appraisalReviewGoal.setAppraisalId(savedReview.getId());
                   appraisalReviewGoal.setReviewerId(role.getReviewerId());
                   appraisalReviewGoal.setReviewerType(role.getReviewerType());
                   appraisalReviewGoal.setGoalId(goalDefinition.getId());
                   appraisalReviewGoal.setComment("");
                   appraisalReviewGoal.setRating("");
                   appraisalReviewGoal.setComplete(false);
                   appraisalReviewGoal.setScore(0.0d);
                   appraisalReviewGoalList.add(appraisalReviewGoal);
               });
           } else if (role.getReviewerType() == AppraisalStatusType.Master) {
               AppraisalReviewMaster appraisalReviewMaster = new AppraisalReviewMaster();
               appraisalReviewMaster.setAppraisalId(savedReview.getId());
               appraisalReviewMaster.setEmployeeId(person.getId());
               appraisalReviewMaster.setReviewerId(role.getReviewerId());
               appraisalReviewMaster.setComment("");
               appraisalReviewMaster.setRating("");
               appraisalReviewMaster.setComplete(false);
               appraisalReviewMasters.add(appraisalReviewMaster);
           }
        });

        roleMap.get(person.getId()).forEach(role -> {
            if (role.getReviewerType() != AppraisalStatusType.Master) {
                countryUnitMap.get(person.getCu()).stream().forEach(goalDefinition -> {
                    AppraisalReviewGoal appraisalReviewGoal = new AppraisalReviewGoal();
                    appraisalReviewGoal.setEmployeeId(person.getId());
                    appraisalReviewGoal.setAppraisalId(savedReview.getId());
                    appraisalReviewGoal.setReviewerId(role.getReviewerId());
                    appraisalReviewGoal.setReviewerType(role.getReviewerType());
                    appraisalReviewGoal.setGoalId(goalDefinition.getId());
                    appraisalReviewGoal.setComment("");
                    appraisalReviewGoal.setRating("");
                    appraisalReviewGoal.setComplete(false);
                    appraisalReviewGoal.setScore(0.0d);
                    appraisalReviewGoalList.add(appraisalReviewGoal);
                });
            }
        });
        reviewGoalRepository.saveAll(appraisalReviewGoalList);
        appraisalReviewMasterRepository.saveAll(appraisalReviewMasters);
    }

    private Map<String, List<AppraisalGoal>> getJobAndGoalMap(List<AppraisalGoal> appraisalGoals) {
        Map<String, List<AppraisalGoal>> goalMap = new HashMap<>();

        for (AppraisalGoal goalDefinition : appraisalGoals) {
            if (goalMap.containsKey(goalDefinition.getJob())) {
                List<AppraisalGoal> goalDefinitionList = goalMap.get(goalDefinition.getJob());
                goalDefinitionList.add(goalDefinition);
                goalMap.put(goalDefinition.getJob(), goalDefinitionList);

            } else {
                List<AppraisalGoal> goalDefinitionList = new ArrayList<>();
                goalDefinitionList.add(goalDefinition);
                goalMap.put(goalDefinition.getJob(), goalDefinitionList);
            }
        }
        return goalMap;
    }

    private Map<String, List<AppraisalGoal>> getCountryUnitAndGoalMap(List<AppraisalGoal> appraisalGoals) {
        Map<String, List<AppraisalGoal>> countryUnitMap = new HashMap<>();

        for (AppraisalGoal goalDefinition : appraisalGoals) {
            if (countryUnitMap.containsKey(goalDefinition.getCu())) {
                List<AppraisalGoal> countryUnitList = countryUnitMap.get(goalDefinition.getCu());
                countryUnitList.add(goalDefinition);
                countryUnitMap.put(goalDefinition.getCu(), countryUnitList);

            } else {
                List<AppraisalGoal> countryUnitList = new ArrayList<>();
                countryUnitList.add(goalDefinition);
                if (goalDefinition.getCu().trim().length() >=1 ) {
                    countryUnitMap.put(goalDefinition.getCu(), countryUnitList);
                }
            }
        }
        return countryUnitMap;
    }

}
