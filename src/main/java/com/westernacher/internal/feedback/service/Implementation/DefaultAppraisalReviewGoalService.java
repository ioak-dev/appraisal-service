package com.westernacher.internal.feedback.service.Implementation;


import com.westernacher.internal.feedback.domain.*;
import com.westernacher.internal.feedback.repository.*;
import com.westernacher.internal.feedback.service.AppraisalReviewGoalService;
import com.westernacher.internal.feedback.util.MailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class DefaultAppraisalReviewGoalService implements AppraisalReviewGoalService {


    @Autowired
    private AppraisalReviewGoalRepository repository;

    @Autowired
    private AppraisalReviewRepository appraisalReviewRepository;

    @Autowired
    private AppraisalGoalRepository appraisalGoalRepository;

    @Autowired
    private AppraisalRoleRepository appraisalRoleRepository;

    @Autowired
    private MailUtil mailUtil;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private AppraisalCycleRepository appraisalCycleRepository;


    @Override
    public List<AppraisalReviewGoal> getReviewGoals(String appraisalId) {
        List<AppraisalReviewGoal> appraisalReviewGoals = repository.findAllByAppraisalId(appraisalId);
        appraisalReviewGoals.sort(
                Comparator.comparing((AppraisalReviewGoal ARG) -> ARG.getReviewerType().ordinal())
        );
        return appraisalReviewGoals;
    }

    @Override
    public List<AppraisalReviewGoal> update(List<AppraisalReviewGoal> reviewGoals) {
        List<AppraisalReviewGoal> newReviewGoals = new ArrayList<>();
        reviewGoals.stream().forEach(appraisalReviewGoal -> {
            AppraisalReviewGoal savedReviewGoal = repository.findById(appraisalReviewGoal.getId()).orElse(null);
            if (savedReviewGoal != null) {
                savedReviewGoal.setEmployeeId(appraisalReviewGoal.getEmployeeId() != null ? appraisalReviewGoal.getEmployeeId() : savedReviewGoal.getEmployeeId());
                savedReviewGoal.setAppraisalId(appraisalReviewGoal.getAppraisalId() != null ? appraisalReviewGoal.getAppraisalId() : savedReviewGoal.getAppraisalId());
                savedReviewGoal.setReviewerId(appraisalReviewGoal.getReviewerId());
                savedReviewGoal.setReviewerType(appraisalReviewGoal.getReviewerType());
                savedReviewGoal.setGoalId(appraisalReviewGoal.getGoalId());
                savedReviewGoal.setComment(appraisalReviewGoal.getComment());
                savedReviewGoal.setRating(appraisalReviewGoal.getRating());
                newReviewGoals.add(repository.save(savedReviewGoal));
            }
        });
        return newReviewGoals;
    }

    @Override
    public List<AppraisalReviewGoal> submit(List<AppraisalReviewGoal> reviewGoals) {

        Map<String, Person> personStore = new HashMap<>();
        List<Person> personList = personRepository.findAll();
        personList.stream().forEach(person -> {
            personStore.put(person.getId(), person);
        });

        List<AppraisalReviewGoal> newReviewGoals = new ArrayList<>();

        double totalScore = 0.0d;

        String appraisalReviewId = reviewGoals.get(0).getAppraisalId();
        String employeeId = reviewGoals.get(0).getEmployeeId();
        AppraisalStatusType type = null;
        String cycleId = "";
        String reviewerId = "";
        List<AppraisalRole> appraisalRoles = new ArrayList<>();

        /*Setting appraisalReviewGoal score and iscomplete attribute*/
        /*if i am submitting list of AppraisalReviewGoal then appraisalId
        (AppraisalReview ID), reviewerType, employeeId will be same for all record*/

        double primaryScore = 0.0d;
        double secondaryScore = 0.0d;

        double primaryWeightage = 0.0d;
        double secondaryWeightage = 0.0d;

        List<AppraisalGoal> appraisalGoals = appraisalGoalRepository.findAllByCuIs("");
        List<String> appraisalGoalIds = new ArrayList<>();
        appraisalGoals.stream().forEach(appraisalGoal -> {
            appraisalGoalIds.add(appraisalGoal.getId());
        });

        for (AppraisalReviewGoal appraisalReviewGoal : reviewGoals) {
            if (appraisalReviewGoal.getRating() != null && appraisalReviewGoal.getRating().length() > 0) {
                AppraisalGoal appraisalGoal = appraisalGoalRepository.findById(appraisalReviewGoal.getGoalId()).orElse(null);
                double weightage = appraisalGoal.getWeightage();
                int rating = Integer.parseInt(appraisalReviewGoal.getRating().trim().substring(0,1));
                appraisalReviewGoal.setScore(weightage * rating);
                if (appraisalGoalIds.contains(appraisalReviewGoal.getGoalId())) {
                    primaryScore = primaryScore + (weightage * rating);
                    primaryWeightage = primaryWeightage + weightage;

                }else {
                    secondaryScore = secondaryScore + (weightage * rating);
                    secondaryWeightage = secondaryWeightage + weightage;
                }
                totalScore = totalScore + (weightage * rating);
            }
            appraisalReviewGoal.setComplete(true);
            newReviewGoals.add(appraisalReviewGoal);

            /*Setting appraisal role totalscore and iscomplete*/
            AppraisalReview appraisalReview = appraisalReviewRepository.findById(appraisalReviewGoal.getAppraisalId()).orElse(null);
            AppraisalRole appraisalRole = appraisalRoleRepository.findByReviewerIdAndEmployeeIdAndCycleIdAndReviewerType(appraisalReviewGoal.getReviewerId(),
                    employeeId, appraisalReview.getCycleId(), appraisalReview.getStatus());

            double primaryNormalizedScore = primaryScore / primaryWeightage;
            if (primaryNormalizedScore != 0 && appraisalRole != null) {
                appraisalRole.setPrimaryScore(Math.round(primaryNormalizedScore * 10) / 10.0);
            } else {
                appraisalRole.setPrimaryScore(0);
            }

            double secondaryNormalizedScore = secondaryScore / secondaryWeightage;
            if (secondaryNormalizedScore != 0 && appraisalRole != null) {
                appraisalRole.setSecondaryScore(Math.round(secondaryNormalizedScore * 10) / 10.0);
            } else {
                appraisalRole.setSecondaryScore(0);
            }

            appraisalRole.setComplete(true);
            appraisalRoles.add(appraisalRoleRepository.save(appraisalRole));
            type = appraisalReview.getStatus();
            cycleId = appraisalReview.getCycleId();
            reviewerId = appraisalReviewGoal.getReviewerId();
        }

        List<AppraisalRole> appraisalRolesDB = appraisalRoleRepository.findByEmployeeIdAndCycleIdAndReviewerType(
                employeeId, cycleId, type
        );
        boolean changeStatus = true;
        //get appraisal and check for all comple value then change status
        for (AppraisalRole appraisalRole : appraisalRolesDB) {
            if (!appraisalRole.isComplete()) {
                changeStatus = false;
            }
        }
        AppraisalReview appraisalReview = appraisalReviewRepository.findById(appraisalReviewId).orElse(null);

        List<AppraisalRole> appraisalRoleList = appraisalRoleRepository.findByEmployeeIdAndCycleId(employeeId, cycleId);

        List<AppraisalStatusType> statusTypes = new ArrayList<>();
        appraisalRoleList.stream().forEach(appraisalRole -> {
            statusTypes.add(appraisalRole.getReviewerType());
        });

        /*Changing appraisal review status to next role*/
        List<AppraisalRole> appraisalRoleListForMail= new ArrayList<>();

        if (appraisalReview != null) {
            if (appraisalReview.getStatus().equals(AppraisalStatusType.Self) && changeStatus == true) {
                if (statusTypes.contains(AppraisalStatusType.Level_1)) {
                    appraisalReview.setStatus(AppraisalStatusType.Level_1);
                } else if(statusTypes.contains(AppraisalStatusType.Level_2)) {
                    appraisalReview.setStatus(AppraisalStatusType.Level_2);
                }else if(statusTypes.contains(AppraisalStatusType.Level_3)) {
                    appraisalReview.setStatus(AppraisalStatusType.Level_3);
                }else if(statusTypes.contains(AppraisalStatusType.Level_4)) {
                    appraisalReview.setStatus(AppraisalStatusType.Level_4);
                }else if(statusTypes.contains(AppraisalStatusType.Master)) {
                    appraisalReview.setStatus(AppraisalStatusType.Master);
                }else {
                    appraisalReview.setStatus(AppraisalStatusType.Complete);
                }

            } else if (appraisalReview.getStatus().equals(AppraisalStatusType.Level_1)&& changeStatus == true) {

                if(statusTypes.contains(AppraisalStatusType.Level_2)) {
                    appraisalReview.setStatus(AppraisalStatusType.Level_2);
                }else if(statusTypes.contains(AppraisalStatusType.Level_3)) {
                    appraisalReview.setStatus(AppraisalStatusType.Level_3);
                }else if(statusTypes.contains(AppraisalStatusType.Level_4)) {
                    appraisalReview.setStatus(AppraisalStatusType.Level_4);
                }else if(statusTypes.contains(AppraisalStatusType.Master)) {
                    appraisalReview.setStatus(AppraisalStatusType.Master);
                }else {
                    appraisalReview.setStatus(AppraisalStatusType.Complete);
                }

            } else if (appraisalReview.getStatus().equals(AppraisalStatusType.Level_2)&& changeStatus == true) {
                if(statusTypes.contains(AppraisalStatusType.Level_3)) {
                    appraisalReview.setStatus(AppraisalStatusType.Level_3);
                }else if(statusTypes.contains(AppraisalStatusType.Level_4)) {
                    appraisalReview.setStatus(AppraisalStatusType.Level_4);
                }else if(statusTypes.contains(AppraisalStatusType.Master)) {
                    appraisalReview.setStatus(AppraisalStatusType.Master);
                }else {
                    appraisalReview.setStatus(AppraisalStatusType.Complete);
                }
            } else if (appraisalReview.getStatus().equals(AppraisalStatusType.Level_3)&& changeStatus == true) {
                if(statusTypes.contains(AppraisalStatusType.Level_4)) {
                    appraisalReview.setStatus(AppraisalStatusType.Level_4);
                }else if(statusTypes.contains(AppraisalStatusType.Master)) {
                    appraisalReview.setStatus(AppraisalStatusType.Master);
                }else {
                    appraisalReview.setStatus(AppraisalStatusType.Complete);
                }
            } else if (appraisalReview.getStatus().equals(AppraisalStatusType.Level_4)&& changeStatus == true) {
                if(statusTypes.contains(AppraisalStatusType.Master)) {
                    appraisalReview.setStatus(AppraisalStatusType.Master);
                }else {
                    appraisalReview.setStatus(AppraisalStatusType.Complete);
                }
            } else if (appraisalReview.getStatus().equals(AppraisalStatusType.Master)&& changeStatus == true) {
                appraisalReview.setStatus(AppraisalStatusType.Complete);
            }
            appraisalReviewRepository.save(appraisalReview);
            appraisalRoleListForMail = appraisalRoleRepository.findByEmployeeIdAndCycleIdAndReviewerType(employeeId,
                    cycleId, appraisalReview.getStatus());
        }
        sendMailAfterSubmit(appraisalRoleListForMail, personStore);
        return repository.saveAll(newReviewGoals);
    }

    @Async
    public void sendMailAfterSubmit(List<AppraisalRole> appraisalRoleListForMail, Map<String, Person> personStore) {

        AppraisalCycle appraisalCycle = appraisalCycleRepository.findById(appraisalRoleListForMail.get(0).getCycleId()).orElse(null);
        Map<AppraisalStatusType, Date> deadline = appraisalCycle.getDeadline();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMMM yyyy");
        //dd MMMMM yyyy

        appraisalRoleListForMail.stream().forEach(appraisalRole -> {
            Person toPerson = personStore.get(appraisalRole.getReviewerId());
            Person fromPerson = personStore.get(appraisalRole.getEmployeeId());

            Map<String, String> body= new HashMap<>();
            body.put("reviewer", fromPerson.getFirstName()+" "+fromPerson.getLastName());
            body.put("employee", toPerson.getFirstName()+" "+toPerson.getLastName());
            body.put("reviewerType", appraisalRole.getReviewerType().name());

            Map<String, String> subject= new HashMap<>();
            subject.put("employee", fromPerson.getFirstName()+" "+fromPerson.getLastName());

            if (toPerson != null && appraisalRole.getReviewerType().equals(AppraisalStatusType.Self)) {
                body.put("deadline", simpleDateFormat.format(deadline.get(AppraisalStatusType.Self)));
                body.put("reviewer", appraisalCycle.getWorkflowMap().get(AppraisalStatusType.Self));
                boolean isSuccess = mailUtil.send(toPerson.getEmail(), "level-self-body.vm", body,
                        "level-self-subject.vm", subject);
            }

            if (toPerson != null && appraisalRole.getReviewerType().equals(AppraisalStatusType.Level_1)) {
                body.put("deadline", simpleDateFormat.format(deadline.get(AppraisalStatusType.Level_1)));
                body.put("reviewer", appraisalCycle.getWorkflowMap().get(AppraisalStatusType.Level_1));
                boolean isSuccess = mailUtil.send(toPerson.getEmail(), "level-one-body.vm", body,
                        "level-one-subject.vm", subject);
            }

            if (toPerson != null && appraisalRole.getReviewerType().equals(AppraisalStatusType.Level_2)) {
                body.put("deadline", simpleDateFormat.format(deadline.get(AppraisalStatusType.Level_2)));
                body.put("reviewer", appraisalCycle.getWorkflowMap().get(AppraisalStatusType.Level_2));
                boolean isSuccess = mailUtil.send(toPerson.getEmail(), "level-two-body.vm", body,
                        "level-two-subject.vm", subject);
            }

            if (toPerson != null && appraisalRole.getReviewerType().equals(AppraisalStatusType.Level_3)) {
                body.put("deadline", simpleDateFormat.format(deadline.get(AppraisalStatusType.Level_3)));
                body.put("reviewer", appraisalCycle.getWorkflowMap().get(AppraisalStatusType.Level_3));
                boolean isSuccess = mailUtil.send(toPerson.getEmail(), "level-three-body.vm", body,
                        "level-three-subject.vm", subject);
            }

            if (toPerson != null && appraisalRole.getReviewerType().equals(AppraisalStatusType.Level_4)) {
                body.put("deadline", simpleDateFormat.format(deadline.get(AppraisalStatusType.Level_4)));
                body.put("reviewer", appraisalCycle.getWorkflowMap().get(AppraisalStatusType.Level_4));
                boolean isSuccess = mailUtil.send(toPerson.getEmail(), "level-four-body.vm", body,
                        "level-four-subject.vm", subject);
            }

            if (toPerson != null && appraisalRole.getReviewerType().equals(AppraisalStatusType.Master)) {
                body.put("deadline", simpleDateFormat.format(deadline.get(AppraisalStatusType.Master)));
                body.put("reviewer", appraisalCycle.getWorkflowMap().get(AppraisalStatusType.Master));
                boolean isSuccess = mailUtil.send(toPerson.getEmail(), "master-body.vm", body,
                        "master-subject.vm", subject);
            }
        });
    }
}


