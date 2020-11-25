package com.westernacher.internal.feedback.service.Implementation;

import com.westernacher.internal.feedback.domain.*;
import com.westernacher.internal.feedback.repository.*;
import com.westernacher.internal.feedback.service.AppraisalCycleService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private AppraisalPersonRepository appraisalPersonRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AppraisalReviewGoalRepository reviewGoalRepository;

    @Autowired
    private AppraisalReviewRepository appraisalReviewRepository;

    @Autowired
    private AppraisalReviewGoalRepository appraisalReviewGoalRepository;

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

        /*Persist appraisal person from person*/
        appraisalPersonRepository.saveAll(getAppraisalPersonFromPerson(personRepository.findAll(), cycle.getId()));

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
                AppraisalReview appraisalReview = createAppraisalreviewGoal(roleMap, goalMap, countryUnitMap, person, cycle.getId());
                createSetGoals(goalMap, countryUnitMap, person, cycle.getId(), AppraisalStatusType.SET_GOAL, appraisalReview);
                createSetGoals(goalMap, countryUnitMap, person, cycle.getId(), AppraisalStatusType.REVIEW_GOAL, appraisalReview);
            }
        });

        return cycle;
    }

    @Override
    public AppraisalCycleResource.CycleDeleteResource delete(String id) {

        List<AppraisalReview> appraisalReviews = appraisalReviewRepository.findAllByCycleId(id);

        List<String> appraisalReviewIds = new ArrayList<>();
        appraisalReviews.stream().forEach(appraisalReview -> {
            appraisalReviewIds.add(appraisalReview.getId());
        });


        repository.deleteById(id);

        AppraisalCycleResource.CycleDeleteResource resource = new AppraisalCycleResource.CycleDeleteResource();
        resource.setDeletedRoles(appraisalRoleRepository.deleteAllByCycleId(id));
        resource.setDeletedGoals(appraisalGoalRepository.deleteAllByCycleId(id));
        resource.setDeletedAppraisalReviewGoals(reviewGoalRepository.deleteAllByAppraisalIdIn(appraisalReviewIds));
        resource.setDeletedAppraisalReviewMasters(appraisalReviewMasterRepository.deleteAllByAppraisalIdIn(appraisalReviewIds));
        resource.setDeletedAppraisalReviews(appraisalReviewRepository.deleteAllByCycleId(id));
        resource.setDeletedCycle(1);

        return resource;

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

    private List<AppraisalPerson> getAppraisalPersonFromPerson(List<Person> Persons, String cycleId) {
        List<AppraisalPerson> appraisalPersonList = new ArrayList<>();
        Persons.stream().forEach(person -> {
            AppraisalPerson appraisalPerson = new AppraisalPerson();
            appraisalPerson.setEmpId(person.getEmpId());
            appraisalPerson.setFirstName(person.getFirstName());
            appraisalPerson.setLastName(person.getLastName());
            appraisalPerson.setJoiningDate(person.getJoiningDate());
            appraisalPerson.setCycleId(cycleId);
            appraisalPerson.setCu(person.getCu());
            appraisalPerson.setJob(person.getJob());
            appraisalPerson.setUnit(person.getUnit());
            appraisalPerson.setStatus(person.getStatus());
            appraisalPerson.setEmail(person.getEmail());
            appraisalPerson.setLastAppraisalDate(person.getLastAppraisalDate());
            appraisalPerson.setDuration(person.getDuration());
            appraisalPersonList.add(appraisalPerson);
        });
        return appraisalPersonList;
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
            appraisalRole.setPrimaryScore(0.0d);
            appraisalRole.setSecondaryScore(0.0d);
            employeeList.add(role.getEmployeeId());
            appraisalRoleList.add(appraisalRole);
        });
        employeeList.stream().forEach(employee-> {
            AppraisalRole appraisalRole = new AppraisalRole();
            appraisalRole.setReviewerId(employee);
            appraisalRole.setReviewerType(AppraisalStatusType.Self.name());
            appraisalRole.setEmployeeId(employee);
            appraisalRole.setCycleId(cycleId);
            appraisalRole.setComplete(false);
            appraisalRole.setPrimaryScore(0.0d);
            appraisalRole.setSecondaryScore(0.0d);
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

    private AppraisalReview createAppraisalreviewGoal(Map<String, List<AppraisalRole>> roleMap, Map<String, List<AppraisalGoal>> goalDefinitionMap,
                                           Map<String, List<AppraisalGoal>> countryUnitMap, Person person, String cycleId) {

        AppraisalReview appraisalReview = new AppraisalReview();
        appraisalReview.setCycleId(cycleId);
        appraisalReview.setEmployeeId(person.getId());
        appraisalReview.setStatus(AppraisalStatusType.Self.name());
        AppraisalReview savedReview = appraisalReviewRepository.save(appraisalReview);

        List<AppraisalReviewGoal> appraisalReviewGoalList = new ArrayList<>();
        List<AppraisalReviewMaster> appraisalReviewMasters = new ArrayList<>();

        roleMap.get(person.getId()).forEach(role -> {
            if (role.getReviewerType() == AppraisalStatusType.Master.name()) {
                AppraisalReviewMaster appraisalReviewMaster = new AppraisalReviewMaster();
                appraisalReviewMaster.setAppraisalId(savedReview.getId());
                appraisalReviewMaster.setEmployeeId(person.getId());
                appraisalReviewMaster.setReviewerId(role.getReviewerId());
                appraisalReviewMaster.setComment("");
                appraisalReviewMaster.setRating("");
                appraisalReviewMaster.setComplete(false);
                appraisalReviewMasters.add(appraisalReviewMaster);
            } else {
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

        return savedReview;
    }

    private void createSetGoals(Map<String, List<AppraisalGoal>> goalDefinitionMap,
                                           Map<String, List<AppraisalGoal>> countryUnitMap, Person person,
                                           String cycleId, AppraisalStatusType appraisalStatusType, AppraisalReview appraisalReview) {

        List<AppraisalReviewGoal> appraisalReviewGoalList = new ArrayList<>();

        goalDefinitionMap.get(person.getJob()).stream().forEach(goalDefinition -> {
            AppraisalReviewGoal appraisalReviewGoal = new AppraisalReviewGoal();
            appraisalReviewGoal.setAppraisalId(appraisalReview.getId());
            appraisalReviewGoal.setEmployeeId(person.getId());
            appraisalReviewGoal.setReviewerType(appraisalStatusType.name());
            appraisalReviewGoal.setGoalId(goalDefinition.getId());
            appraisalReviewGoal.setComment("");
            appraisalReviewGoalList.add(appraisalReviewGoal);
        });
        countryUnitMap.get(person.getCu()).stream().forEach(goalDefinition -> {
            AppraisalReviewGoal appraisalReviewGoal = new AppraisalReviewGoal();
            appraisalReviewGoal.setAppraisalId(appraisalReview.getId());
            appraisalReviewGoal.setEmployeeId(person.getId());
            appraisalReviewGoal.setReviewerType(appraisalStatusType.name());
            appraisalReviewGoal.setGoalId(goalDefinition.getId());
            appraisalReviewGoal.setComment("");
            appraisalReviewGoalList.add(appraisalReviewGoal);
        });

        reviewGoalRepository.saveAll(appraisalReviewGoalList);
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

    @Override
    public void copyPreviousAppraisalGoals(String sourceCycleId, String destinationCycleId) {

        log.info(sourceCycleId + " to " + destinationCycleId);
        Map<String, Person> personMap = new HashMap<>();
        personRepository.findAll().forEach(item -> {
            personMap.put(item.getId(), item);
        });

        Map<String, String> sourceGoalReferenceMap = new HashMap();
        appraisalGoalRepository.findAllByCycleId(sourceCycleId).stream().forEach(appraisalGoal -> {
            if (appraisalGoal.getJob() != null && !appraisalGoal.getJob().isEmpty()) {
                sourceGoalReferenceMap.put(appraisalGoal.getId(), "JOB::"
                        + textToIdentifier(appraisalGoal.getJob())
                        + "::"
                        + textToIdentifier(appraisalGoal.getGroup())
                        + "::"
                        + textToIdentifier(appraisalGoal.getCriteria()));
            } else if (appraisalGoal.getCu() != null && !appraisalGoal.getCu().isEmpty()) {
                sourceGoalReferenceMap.put(appraisalGoal.getId(), "CU::"
                        + textToIdentifier(appraisalGoal.getCu().toLowerCase())
                        + "::"
                        + textToIdentifier(appraisalGoal.getGroup())
                        + "::"
                        + textToIdentifier(appraisalGoal.getCriteria()));
            }
        });

        List<String> sourceAppraisalIdList = new ArrayList();
        List<AppraisalReview> sourceAppraisalList = appraisalReviewRepository.findAllByCycleId(sourceCycleId);
        sourceAppraisalList.stream().forEach(appraisalReview -> {
            sourceAppraisalIdList.add(appraisalReview.getId());
        });

        List<AppraisalReviewGoal> appraisalReviewGoalLists = appraisalReviewGoalRepository.findAllByAppraisalIdInAndReviewerType(sourceAppraisalIdList, AppraisalStatusType.REVIEW_GOAL);
        Map<String, AppraisalReviewGoal> sourceSetGoalsMap = new HashMap<>();
        appraisalReviewGoalLists.stream().forEach(appraisalReviewGoal -> {
            sourceSetGoalsMap.put(appraisalReviewGoal.getEmployeeId()+"::"+sourceGoalReferenceMap.get(appraisalReviewGoal.getGoalId()), appraisalReviewGoal);
        });

        Map<String, String> destGoalReferenceMap = new HashMap();
        List<AppraisalGoal> appraisalGoalListDest = appraisalGoalRepository.findAllByCycleId(destinationCycleId);
        appraisalGoalListDest.stream().forEach(appraisalGoal -> {
            if (appraisalGoal.getJob() != null && !appraisalGoal.getJob().isEmpty()) {
                destGoalReferenceMap.put("JOB::"
                        + textToIdentifier(appraisalGoal.getJob())
                        + "::"
                        + textToIdentifier(appraisalGoal.getGroup())
                        + "::"
                        + textToIdentifier(appraisalGoal.getCriteria()), appraisalGoal.getId());
            } else if (appraisalGoal.getCu() != null && !appraisalGoal.getCu().isEmpty()) {
                destGoalReferenceMap.put("CU::"
                        + textToIdentifier(appraisalGoal.getCu().toLowerCase())
                        + "::"
                        + textToIdentifier(appraisalGoal.getGroup())
                        + "::"
                        + textToIdentifier(appraisalGoal.getCriteria()), appraisalGoal.getId());
            }
        });

        Map<String, String> destEmployeeIdToAppraisalIdMap = new HashMap<>();
        List<AppraisalReview> destAppraisalList = appraisalReviewRepository.findAllByCycleId(destinationCycleId);
        destAppraisalList.stream().forEach(appraisalReview -> {
            destEmployeeIdToAppraisalIdMap.put(appraisalReview.getEmployeeId(), appraisalReview.getId());
        });

        List<AppraisalReviewGoal> appraisalReviewGoalList = new ArrayList<>();

        sourceSetGoalsMap.keySet().stream().forEach(p -> {
            String[] splitsValue = p.split("::");
            String employeeId = splitsValue[0];
            if (personMap.containsKey(employeeId)) {
                String goalKey = splitsValue[1] + "::"
                        + textToIdentifier(splitsValue[1].equals("JOB") ? personMap.get(employeeId).getJob() : personMap.get(employeeId).getCu())
                        + "::" + splitsValue[3] + "::" + splitsValue[4];
                if (destGoalReferenceMap.containsKey(goalKey) && destEmployeeIdToAppraisalIdMap.containsKey(employeeId)) {
                    String goalId = destGoalReferenceMap.get(goalKey);
                    String appraisalId = destEmployeeIdToAppraisalIdMap.get(employeeId);
                    AppraisalReviewGoal appraisalReviewGoal = new AppraisalReviewGoal();
                    appraisalReviewGoal.setEmployeeId(sourceSetGoalsMap.get(p).getEmployeeId());
                    appraisalReviewGoal.setAppraisalId(appraisalId);
                    appraisalReviewGoal.setReviewerType(AppraisalStatusType.SET_GOAL.name());
                    appraisalReviewGoal.setGoalId(goalId);
                    appraisalReviewGoal.setComment(sourceSetGoalsMap.get(p).getComment());
                    appraisalReviewGoal.setRating(sourceSetGoalsMap.get(p).getRating());
                    appraisalReviewGoal.setComplete(sourceSetGoalsMap.get(p).isComplete());
                    appraisalReviewGoal.setScore(sourceSetGoalsMap.get(p).getScore());
                    appraisalReviewGoalList.add(appraisalReviewGoal);
                }
            }
        });

        Set<String> appraisalIdList = appraisalReviewGoalList.stream().map(item -> item.getAppraisalId()).collect(Collectors.toSet());

        appraisalReviewGoalRepository.deleteAllByAppraisalIdInAndReviewerType(appraisalIdList, AppraisalStatusType.SET_GOAL);
        appraisalReviewGoalRepository.saveAll(appraisalReviewGoalList);
    }

    private String textToIdentifier(String input) {
        if (input == null) {
            return "";
        }
        return input.toLowerCase().replaceAll("\\s", "");
    }

}
