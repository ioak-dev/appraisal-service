package com.westernacher.internal.feedback.service.Implementation;


import com.westernacher.internal.feedback.domain.*;
import com.westernacher.internal.feedback.repository.*;
import com.westernacher.internal.feedback.service.AppraisalReviewGoalService;
import com.westernacher.internal.feedback.util.MailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        for (AppraisalReviewGoal appraisalReviewGoal : reviewGoals) {
            if (appraisalReviewGoal.getRating() != null && appraisalReviewGoal.getRating().length() > 0) {
                AppraisalGoal appraisalGoal = appraisalGoalRepository.findById(appraisalReviewGoal.getGoalId()).orElse(null);
                double weightage = appraisalGoal.getWeightage();
                int rating = Integer.parseInt(appraisalReviewGoal.getRating().trim().substring(0,1));
                appraisalReviewGoal.setScore(weightage * rating);
                totalScore = totalScore + (weightage * rating);
            }
            appraisalReviewGoal.setComplete(true);
            newReviewGoals.add(appraisalReviewGoal);

            /*Setting appraisal role totalscore and iscomplete*/
            AppraisalReview appraisalReview = appraisalReviewRepository.findById(appraisalReviewGoal.getAppraisalId()).orElse(null);
            AppraisalRole appraisalRole = appraisalRoleRepository.findByReviewerIdAndEmployeeIdAndCycleIdAndReviewerType(appraisalReviewGoal.getReviewerId(),
                    employeeId, appraisalReview.getCycleId(), appraisalReview.getStatus());

            appraisalRole.setTotalScore(Math.round(totalScore * 10) / 10.0);
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

        appraisalRoleListForMail.stream().forEach(appraisalRole -> {
            Person toPerson = personRepository.findById(appraisalRole.getReviewerId()).orElse(null);
            Person fromPerson = personRepository.findById(appraisalRole.getEmployeeId()).orElse(null);

            Map<String, String> body= new HashMap<>();
            body.put("reviewer", toPerson.getFirstName()+" "+toPerson.getLastName());
            body.put("employee", fromPerson.getFirstName()+" "+fromPerson.getLastName()+"("+fromPerson.getEmail()+")");
            body.put("reviewerType", appraisalRole.getReviewerType().name());

            Map<String, String> subject= new HashMap<>();
            subject.put("employee", fromPerson.getFirstName()+" "+fromPerson.getLastName());

            if (toPerson != null) {
                mailUtil.send(toPerson.getEmail(), "appraisal-review-body.vm", body,
                        "appraisal-review-subject.vm", subject);
            }
        });

        return repository.saveAll(newReviewGoals);
    }
}


