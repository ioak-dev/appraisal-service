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
    private AppraisalReviewRepository reviewRepository;

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
        String reviewerId = reviewGoals.get(0).getReviewerId();
        String employeeId = reviewGoals.get(0).getEmployeeId();

        for (AppraisalReviewGoal appraisalReviewGoal : reviewGoals) {
            appraisalReviewGoal.setComplete(true);
            int rating = appraisalReviewGoal.getRating().charAt(0);
            AppraisalGoal appraisalGoal = appraisalGoalRepository.findById(appraisalReviewGoal.getGoalId()).orElse(null);
            appraisalReviewGoal.setScore((rating * appraisalGoal.getWeightage()) / 100);
            totalScore = totalScore + (rating * appraisalGoal.getWeightage()) / 100;
            newReviewGoals.add(appraisalReviewGoal);
        }


        AppraisalReview appraisalReview = reviewRepository.findById(appraisalReviewId).orElse(null);
        AppraisalRole appraisalRole = appraisalRoleRepository.findByReviewerIdAndEmployeeIdAndCycleIdAndReviewerType(reviewerId,
                employeeId, appraisalReview.getCycleId(), appraisalReview.getStatus());

        appraisalRole.setTotalScore(totalScore);
        appraisalRole.setComplete(true);
        appraisalRoleRepository.save(appraisalRole);

        if (appraisalReview != null) {
            if (appraisalReview.getStatus().equals(AppraisalStatusType.SELF_APPRAISAL)) {
                appraisalReview.setStatus(AppraisalStatusType.PROJECT_MANAGER);
            } else if (appraisalReview.getStatus().equals(AppraisalStatusType.PROJECT_MANAGER)) {
                appraisalReview.setStatus(AppraisalStatusType.REPORTING_MANAGER);
            } else if (appraisalReview.getStatus().equals(AppraisalStatusType.REPORTING_MANAGER)) {
                appraisalReview.setStatus(AppraisalStatusType.PRACTICE_DIRECTOR);
            } else if (appraisalReview.getStatus().equals(AppraisalStatusType.PRACTICE_DIRECTOR)) {
                appraisalReview.setStatus(AppraisalStatusType.HR);
            }
            reviewRepository.save(appraisalReview);
        }
        return repository.saveAll(newReviewGoals);
    }
}


