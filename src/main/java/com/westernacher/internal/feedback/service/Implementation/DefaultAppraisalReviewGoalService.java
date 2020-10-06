package com.westernacher.internal.feedback.service.Implementation;


import com.westernacher.internal.feedback.domain.*;
import com.westernacher.internal.feedback.repository.AppraisalGoalRepository;
import com.westernacher.internal.feedback.repository.AppraisalReviewGoalRepository;
import com.westernacher.internal.feedback.repository.AppraisalReviewRepository;
import com.westernacher.internal.feedback.repository.AppraisalRoleRepository;
import com.westernacher.internal.feedback.service.AppraisalReviewGoalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

        /*Changing appraisal review status to next role*/
        if (appraisalReview != null) {
            if (appraisalReview.getStatus().equals(AppraisalStatusType.Self) && changeStatus == true) {
                appraisalReview.setStatus(AppraisalStatusType.Level_1);
            } else if (appraisalReview.getStatus().equals(AppraisalStatusType.Level_1)&& changeStatus == true) {
                appraisalReview.setStatus(AppraisalStatusType.Level_2);
            } else if (appraisalReview.getStatus().equals(AppraisalStatusType.Level_2)&& changeStatus == true) {
                appraisalReview.setStatus(AppraisalStatusType.Level_3);
            } else if (appraisalReview.getStatus().equals(AppraisalStatusType.Level_3)&& changeStatus == true) {
                appraisalReview.setStatus(AppraisalStatusType.Level_4);
            }
            appraisalReviewRepository.save(appraisalReview);
        }
        return repository.saveAll(newReviewGoals);
    }
}


