package com.westernacher.internal.feedback.service.Implementation;

import com.westernacher.internal.feedback.domain.*;
import com.westernacher.internal.feedback.domain.v2.AppraisalRole;
import com.westernacher.internal.feedback.domain.v2.Person;
import com.westernacher.internal.feedback.repository.*;
import com.westernacher.internal.feedback.repository.v2.PersonRepository;
import com.westernacher.internal.feedback.service.AppraisalCycleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class DefaultAppraisalCycleService implements AppraisalCycleService {

    @Autowired
    private AppraisalCycleRepository repository;

    @Autowired
    private v1GoalRepository v1GoalRepository;

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
    private v1AppraisalRoleRepository v1AppraisalRoleRepository;

    @Autowired
    private AppraisalReviewMasterRepository appraisalReviewMasterRepository;

    @Value("${zip.file.name}")
    private String zippedFile;

    public AppraisalCycle create(AppraisalCycle appraisalCycle) {

        appraisalCycle.setStatus(AppraisalCycleStatusType.ACTIVE);
        AppraisalCycle cycle = repository.save(appraisalCycle);

        /*Persist appraisal goal from goal*/
        appraisalGoalRepository.saveAll(getAppraisalGoalFromGoal(v1GoalRepository.findAll(), cycle.getId()));

        /*Persist appraisal person from person*/
        appraisalPersonRepository.saveAll(getAppraisalPersonFromPerson(personRepository.findAll(), cycle.getId()));

        List<Person> personList = personRepository.findAllByCu(cycle.getCu());

        List<String> personIdList = new ArrayList<>();

        personList.stream().forEach(person -> {
            personIdList.add(person.getId());
        });

        /*persist appraisal role from role*/
        v1AppraisalRoleRepository.saveAll(getAppraisalRoleFromRole(roleRepository.findAllByEmployeeIdIn(personIdList), cycle.getId()));

        List<AppraisalGoal> appraisalGoals = appraisalGoalRepository.findAllByCycleId(cycle.getId());

        /*Map of jobs with respective goals*/
        Map<String, List<AppraisalGoal>> goalMap = getJobAndGoalMap(appraisalGoals);

        /*Map of country unit and their respective goals*/
        Map<String, List<AppraisalGoal>> countryUnitMap = getCountryUnitAndGoalMap(appraisalGoals);

        /*Map of only employees and their respective roles*/
        Map<String, List<AppraisalRole>> roleMap = getEmployeeAndAppraisalRoleMap(v1AppraisalRoleRepository.findAllByCycleId(cycle.getId()));

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
        resource.setDeletedRoles(v1AppraisalRoleRepository.deleteAllByCycleId(id));
        resource.setDeletedGoals(appraisalGoalRepository.deleteAllByCycleId(id));
        resource.setDeletedAppraisalReviewGoals(reviewGoalRepository.deleteAllByAppraisalIdIn(appraisalReviewIds));
        resource.setDeletedAppraisalReviewMasters(appraisalReviewMasterRepository.deleteAllByAppraisalIdIn(appraisalReviewIds));
        resource.setDeletedAppraisalReviews(appraisalReviewRepository.deleteAllByCycleId(id));
        resource.setDeletedCycle(1);

        return resource;

    }

    private List<AppraisalGoal> getAppraisalGoalFromGoal(List<v1Goal> v1Goals, String cycleId) {
        List<AppraisalGoal> appraisalGoalList = new ArrayList<>();
        v1Goals.stream().forEach(goal -> {
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
            if (role.getReviewerType().equals(AppraisalStatusType.Master.name())) {
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

    @Override
    public List<String> movetonextlevel(String cycleId, String currentLevel, String employeeId, boolean moveBackwards) {
        List<String> updatedPersons = new ArrayList<>();
        List<AppraisalReview> appraisalReviewList = null;
        if (employeeId != null) {
            appraisalReviewList = appraisalReviewRepository.findAllByCycleIdAndEmployeeIdIn(cycleId, Arrays.asList(employeeId));
        } else if (currentLevel != null) {
            appraisalReviewList = appraisalReviewRepository.findAllByCycleIdAndStatus(cycleId, currentLevel);
        } else {
            return updatedPersons;
        }

        List<AppraisalRole> appraisalRoleList = v1AppraisalRoleRepository.findAllByCycleId(cycleId);
        Map<String, List<String>> roleMap = new HashMap<>();
        appraisalRoleList.forEach(appraisalRole -> {
            if (!appraisalRole.isComplete()) {
                if (roleMap.containsKey(appraisalRole.getEmployeeId())) {
                    roleMap.get(appraisalRole.getEmployeeId()).add(appraisalRole.getReviewerType());
                } else {
                    List<String> list = new ArrayList<>();
                    list.add(appraisalRole.getReviewerType());
                    roleMap.put(appraisalRole.getEmployeeId(), list);
                }
            }
        });

        Map<String, Person> personMap = new HashMap<>();
        personRepository.findAll().forEach(item -> {
            personMap.put(item.getId(), item);
        });

        appraisalReviewList.forEach(appraisalReview -> {
            if (updateAppraisalReviewStatus(appraisalReview, roleMap.get(appraisalReview.getEmployeeId()), moveBackwards)) {
                updatedPersons.add(personMap.get(appraisalReview.getEmployeeId()).getEmail());
            }
        });

        return updatedPersons;
    }


    private boolean updateAppraisalReviewStatus(AppraisalReview appraisalReview, List<String> availableStatusList, boolean moveBackwards) {
        List<AppraisalStatusType> allStatusList = Arrays.asList(
                AppraisalStatusType.Self,
                AppraisalStatusType.Level_1,
                AppraisalStatusType.Level_2,
                AppraisalStatusType.Level_3,
                AppraisalStatusType.Level_4,
                AppraisalStatusType.Master
        );
        if(moveBackwards) {
            Collections.reverse(allStatusList);
        }
        if (appraisalReview.getStatus().equals((moveBackwards ? AppraisalStatusType.Self : AppraisalStatusType.Master).name())) {
            return false;
        }
        int index = allStatusList.indexOf(AppraisalStatusType.valueOf(appraisalReview.getStatus()));
        List<AppraisalStatusType> targetStatusList =  allStatusList.subList(index + 1, allStatusList.size());
        boolean outcome = false;
        for (AppraisalStatusType targetStatus : targetStatusList) {
            if (availableStatusList.contains(targetStatus.name())) {
                appraisalReview.setStatus(targetStatus.name());
                appraisalReviewRepository.save(appraisalReview);
                outcome = true;
                break;
            }
        }

        return outcome;
    }

    private String textToIdentifier(String input) {
        if (input == null) {
            return "";
        }
        return input.toLowerCase().replaceAll("\\s", "");
    }

    @Override
    public void printPdf(HttpServletResponse response, List<String> appraisalReviewIds) {
        try{
            Path tmpFilePath = Files.createTempDirectory("appraisal-");
            appraisalReviewIds.forEach(id->{
                Map<String, Person> personMap = new HashMap<>();
                personRepository.findAll().forEach(item -> {
                    personMap.put(item.getId(), item);
                });

                AppraisalReview appraisalReview = appraisalReviewRepository.findById(id).orElse(null);

                AppraisalCycle appraisalCycle = repository.findById(appraisalReview.getCycleId()).orElse(null);

                Person person = personMap.get(appraisalReview.getEmployeeId());

                List<AppraisalRole> appraisalRoleList = v1AppraisalRoleRepository.findAllByEmployeeId(appraisalReview.getEmployeeId());

                appraisalRoleList.sort(
                        Comparator.comparing((AppraisalRole ARG) -> AppraisalStatusType.valueOf(ARG.getReviewerType()).ordinal())
                );

                List<AppraisalGoal> appraisalGoals = appraisalGoalRepository.findAllByCycleId(appraisalReview.getCycleId());


                List<Report.CriteriaDetails> criteriaDetails = getCriteriaDetails(appraisalGoals, personMap, appraisalReview);


                Map<String, List<Report.CriteriaDetails>> groupMap = new LinkedHashMap<>();

                for(Report.CriteriaDetails criteriaDetail : criteriaDetails) {
                    List<Report.CriteriaDetails> criteriaDetailsList;

                    if(groupMap.containsKey(criteriaDetail.getGroupName())) {
                        criteriaDetailsList = groupMap.get(criteriaDetail.getGroupName());
                    }else {
                        criteriaDetailsList = new ArrayList<>();
                    }
                    criteriaDetailsList.add(criteriaDetail);
                    groupMap.put(criteriaDetail.getGroupName(), criteriaDetailsList);
                }

                List<Report.ReportDetails> reportBody = new LinkedList<>();

                for (Map.Entry<String, List<Report.CriteriaDetails>> entry : groupMap.entrySet()) {
                    Report.ReportDetails reportDetail = new Report.ReportDetails();
                    reportDetail.setGroupName(entry.getKey());
                    reportDetail.setCriteriaDetails(entry.getValue());
                    reportBody.add(reportDetail);
                }

                List<AppraisalReviewMaster> appraisalReviewMasters = appraisalReviewMasterRepository.findAllByAppraisalId(appraisalReview.getId());

                StringBuffer htmlContent =  createReport(reportBody,
                        getHeader(appraisalCycle.getName(), person, appraisalRoleList, personMap, appraisalCycle),
                        createRatingSummary(appraisalRoleList, personMap, appraisalCycle),
                        createDiscusionSummary(appraisalReviewMasters, personMap), appraisalCycle);
                String xhtml = htmlToXhtml(htmlContent.toString());
                xhtmlToPdf(xhtml, tmpFilePath, person.getFirstName(), person.getLastName());
            });
            //return zipFiles(response, tmpFilePath);

        }catch (IOException e){
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private List<Report.CriteriaDetails> getCriteriaDetails(List<AppraisalGoal> appraisalGoals,
                                                            Map<String, Person> personMap,
                                                            AppraisalReview appraisalReview) {
        List<Report.CriteriaDetails> criteriaDetails = new LinkedList<>();

        for(AppraisalGoal appraisalGoal : appraisalGoals) {
            List<AppraisalReviewGoal> appraisalReviewGoals = appraisalReviewGoalRepository.findAllByGoalIdAndEmployeeId(appraisalGoal.getId(),
                    appraisalReview.getEmployeeId());
            List<Report.PersonDetails> personDetails = new ArrayList<>();
            Report.CriteriaDetails criteriaDetail = new Report.CriteriaDetails();
            for(AppraisalReviewGoal appraisalReviewGoal : appraisalReviewGoals) {
                if(appraisalReviewGoal.getReviewerType().equals("REVIEW_GOAL")) {
                    criteriaDetail.setReviewGoal(appraisalReviewGoal.getComment());
                } else if(appraisalReviewGoal.getReviewerType().equals("SET_GOAL")) {
                    criteriaDetail.setSetGoal(appraisalReviewGoal.getComment());
                } else {
                    Report.PersonDetails personDetail = new Report.PersonDetails();
                    personDetail.setPersonName(personMap.get(appraisalReviewGoal.getReviewerId()).getFirstName()
                            +" "+personMap.get(appraisalReviewGoal.getReviewerId()).getLastName());
                    personDetail.setPosition(appraisalReviewGoal.getReviewerType());
                    personDetail.setRating(appraisalReviewGoal.getRating());
                    personDetail.setComment(appraisalReviewGoal.getComment());
                    personDetails.add(personDetail);
                }
            }
            criteriaDetail.setGroupName(appraisalGoal.getGroup());
            criteriaDetail.setCriteriaName(appraisalGoal.getCriteria());
            criteriaDetail.setCriteriaDescription(appraisalGoal.getDescription());
            criteriaDetail.setWeightage(Float.toString(appraisalGoal.getWeightage()));
            criteriaDetail.setPersonDetails(personDetails);
            criteriaDetails.add(criteriaDetail);
        }
        return criteriaDetails;
    }

    private StringBuffer getHeader(String cycleName,
                             Person person,
                             List<AppraisalRole> approsalRoleList,
                             Map<String, Person> personMap,
                             AppraisalCycle approsalCycle) {
        StringBuffer header = new StringBuffer();
        header.append("<h3 style=\"border-bottom: 1px solid black; padding-bottom: 20px; margin-top: 20px; margin-bottom: 20px;\">Annual Review ");
        header.append(cycleName);
        header.append(" and Target Discussion</h3>");
        //header.append("<h3>Master Data:</h3>");
        header.append("<p>Name of Reviewee: <u>"+ person.getFirstName()+ " "+person.getLastName()+"</u></p>");
        header.append("<p>Level: <u>"+ person.getJob()+"</u></p>");
        header.append("<p>Practice: <u>"+ person.getUnit()+"</u></p>");
        header.append("<p>CU: <u>"+ person.getCu()+"</u></p>");
        header.append("<br/>");

        Map<String, List<String>> contributerMap = new LinkedHashMap<>();

        for(AppraisalRole approsalRole: approsalRoleList) {
            if(!approsalRole.getReviewerType().equals("Self")) {
                if(contributerMap.containsKey(approsalRole.getReviewerType())) {
                    List<String> list = contributerMap.get(approsalRole.getReviewerType());
                    list.add(personMap.get(approsalRole.getReviewerId()).getFirstName()+" "+personMap.get(approsalRole.getReviewerId()).getLastName());
                }else {
                    List<String> list = new ArrayList<>();
                    list.add(personMap.get(approsalRole.getReviewerId()).getFirstName()+" "+personMap.get(approsalRole.getReviewerId()).getLastName());
                    contributerMap.put(approsalRole.getReviewerType(), list);
                }
            }
        }
        for (Map.Entry<String, List<String>> entry : contributerMap.entrySet()) {
            if (approsalCycle.getWorkflowMap().get(AppraisalStatusType.valueOf(entry.getKey())).equalsIgnoreCase("master")) {
                header.append("<p>Review Master");
                header.append("s: <u>"+entry.getValue().stream().collect(Collectors.joining(", "))+"</u></p>");
            } else {
                header.append("<p>"+approsalCycle.getWorkflowMap().get(AppraisalStatusType.valueOf(entry.getKey())));
                header.append("s: <u>"+entry.getValue().stream().collect(Collectors.joining(", "))+"</u></p>");
            }

        }
        header.append("<br/><br/>");
        return header;
    }
    private StringBuffer createDiscusionSummary(List<AppraisalReviewMaster> appraisalReviewMasters, Map<String, Person> personMap) {
        StringBuffer response =  new StringBuffer();
        response.append("<h3>Discussion Summary:</h3>");
        appraisalReviewMasters.stream().forEach(appraisalReviewMaster -> {
            response.append("<h5>"+personMap.get(appraisalReviewMaster.getReviewerId()).getFirstName()+" "+
                    personMap.get(appraisalReviewMaster.getReviewerId()).getLastName()+"</h5>");
            //response.append("<span style=\"float: right\">");
            response.append("<p><i>");
            response.append(appraisalReviewMaster.getRating());
            response.append("</i></p>");
            response.append("<p>"+appraisalReviewMaster.getComment()+"</p>");
            //response.append("<p>"+appraisalReviewMaster.getRating()+"</p>");
        });
        response.append("<br></br>");
        response.append("<br></br>");
        response.append("<br></br>");
        response.append("<h3>Signatures:</h3>");
        response.append("<br></br>");
        response.append("<p>______________</p>");
        response.append("<p>Reviewee</p>");
        response.append("<br></br>");
        response.append("<br></br>");
        response.append("<p>______________</p>");
        response.append("<p>Reviewer (manager)</p>");

        return response;

    }

    private StringBuffer createRatingSummary(List<AppraisalRole> approsalRoleList, Map<String, Person> personMap, AppraisalCycle cycle) {

        StringBuffer response =  new StringBuffer();
        response.append("<h3>Rating  Summary:</h3>");
        approsalRoleList.stream().forEach(appraisalReviewMaster -> {
            if(!appraisalReviewMaster.getReviewerType().equals("Master")) {
                response.append("<h5>"+personMap.get(appraisalReviewMaster.getReviewerId()).getFirstName()+" "+
                        personMap.get(appraisalReviewMaster.getReviewerId()).getLastName());

                if(appraisalReviewMaster.getReviewerType().equals("Self")) {
                    response.append(" As Self Appraisal</h5>");
                }else {
                    response.append(" As " + cycle.getWorkflowMap().get(AppraisalStatusType.valueOf(appraisalReviewMaster.getReviewerType()))+"</h5>");
                }

                response.append("<p>"+"Goals and objectives - "+appraisalReviewMaster.getPrimaryScore()+"</br>");
                response.append("Notable Contributions - "+appraisalReviewMaster.getSecondaryScore()+"</p>");
            }
        });

        return response;

    }

    public StringBuffer createReport(List<Report.ReportDetails> reportDetails, StringBuffer header, StringBuffer ratingSummary, StringBuffer summary, AppraisalCycle cycle) {
        StringBuffer response = new StringBuffer();
        response.append("<html><head><link rel=\"preconnect\" href=\"https://fonts.gstatic.com\">");
        response.append("<link href=\"https://fonts.googleapis.com/css2?family=Open+Sans:ital,wght@0,300;0,400;0,600;1,300;1,400;1,600&display=swap\" rel=\"stylesheet\">");
        response.append("<style>*{font-family: 'Open Sans', sans-serif;}p {text-align: justify; margin: 0; padding: 0; margin-bottom: 10px; font-size: 14px;}h1, h2, h3, h4, h5, h6 {margin: 0; padding: 0;}body {margin: 0 50px;}</style>");
        response.append("<title>Appraisal</title>");
        response.append("</head>");
        response.append("<body>");
        response.append(header);
        reportDetails.stream().forEach(report->{
            response.append(createGroupDetails(report.getGroupName(), report.getCriteriaDetails(), cycle));
        });
        response.append(ratingSummary);
        response.append(summary);
        response.append("</body></html>");
        return response;
    }

    public StringBuffer createGroupDetails(String groupName, List<Report.CriteriaDetails> criteriaDetails, AppraisalCycle cycle) {
        StringBuffer response = new StringBuffer();
        response.append("<div style=\"page-break-before: always;\"><h3>"+groupName);
        response.append("</h3>");

        //Criteria Details
        criteriaDetails.stream().forEach(criteria->{
            if (criteria.getPersonDetails().size()>0) {
                response.append(createCriteriaDetails(criteria.getCriteriaName(), criteria.getWeightage(),
                        criteria.getCriteriaDescription(), criteria.getReviewGoal(), criteria.getSetGoal(), criteria.getPersonDetails(), cycle));
            }
        });

        response.append("</div>");
        return response;
    }

    public StringBuffer createCriteriaDetails(String criteriaName, String weightage,String criteriaDescription,
                                              String reviewGoal, String setGoal, List<Report.PersonDetails> personDetails, AppraisalCycle cycle) {
        StringBuffer response = new StringBuffer();
        response.append("<div><h4>");
        response.append(criteriaName);
        response.append("</h4><p>");
        try {
            response.append(new String(criteriaDescription.getBytes(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        response.append("</p>");

        //Review Goal
        if(reviewGoal != null && !reviewGoal.isEmpty()) {
            response.append("<div>");
            response.append("<h5>Targets for current year</h5><p>");
            response.append(reviewGoal);
            response.append("</p></div>");
        }


        //Person Details
        personDetails.sort(
                Comparator.comparing((Report.PersonDetails ARG) -> AppraisalStatusType.valueOf(ARG.getPosition()).ordinal())
        );
        personDetails.stream().forEach(person->{
            response.append(createPersonDetails(person.getPersonName(), person.getPosition(), person.getComment(), person.getRating(), cycle));
        });

        //Set goals
        if(setGoal != null && !setGoal.isEmpty()) {
            response.append("<div>");
            response.append("<h5>Targets for next year</h5><p>");
            response.append(setGoal);
            response.append("</p></div>");
            response.append("</div>");
        }

        return response;
    }
    public StringBuffer createPersonDetails(String personName, String position, String comment, String rating, AppraisalCycle cycle) {
        StringBuffer response = new StringBuffer();

        if(position.equals("Self")) {
            response.append("<div>");
            response.append("<h5>");
            response.append("Self Appraisal");
            response.append("<span style=\"float: right\">");
            response.append("Self Evaluation: "+rating);
            response.append("</span></h5><p>");
            response.append(comment);
            response.append("</p></div>");
        } else if (position.equals("Level_2")){
            response.append("<div>");
            response.append("<h5>");
            response.append(personName);
            response.append(" As ");
            response.append(cycle.getWorkflowMap().get(AppraisalStatusType.valueOf(position)));
            response.append("<span style=\"float: right\">");
            response.append("Manager Evaluation: "+rating);
            response.append("</span></h5><p>");
            response.append(comment);
            response.append("</p></div>");
        }
        return response;
    }

    private static String htmlToXhtml(String html) {
        Document document = Jsoup.parse(html);
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        log.info("Conversion to XHTML Successful");
        return document.html();
    }

    private static void xhtmlToPdf(String xhtml,Path tmpFilePath, String firstName, String lastName) {
        try{
            File output = new File(firstName+ " "+lastName+".pdf");
            ITextRenderer iTextRenderer = new ITextRenderer();
            iTextRenderer.setDocumentFromString(xhtml);
            iTextRenderer.layout();
            OutputStream os = new FileOutputStream(tmpFilePath.toFile().getAbsolutePath() + File.separator + output);
            iTextRenderer.createPDF(os);
            os.close();
            log.info("Conversion to "+ output + " Successful");
        }
        catch (IOException e){
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error converting to PDF");
        }
    }

    public byte[] zipFiles(HttpServletResponse response, Path tmpFilePath) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.addHeader("Content-Disposition", "attachment; filename="+zippedFile);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
        ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream);
        try{
            List<File> files = Files.list(Paths.get(tmpFilePath.toFile().getAbsolutePath()))
                    .map(Path::toFile).collect(Collectors.toList());
            files.forEach(file -> {
                try {
                    zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
                    FileInputStream fileInputStream = new FileInputStream(file);
                    IOUtils.copy(fileInputStream, zipOutputStream);
                    fileInputStream.close();
                    zipOutputStream.closeEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            });
            zipOutputStream.finish();
            zipOutputStream.flush();
            IOUtils.closeQuietly(zipOutputStream);
            IOUtils.closeQuietly(bufferedOutputStream);
            IOUtils.closeQuietly(byteArrayOutputStream);
            log.info("Compression and Zipping of PDF Successful");
        } catch (IOException e){
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return byteArrayOutputStream.toByteArray();
    }
}
