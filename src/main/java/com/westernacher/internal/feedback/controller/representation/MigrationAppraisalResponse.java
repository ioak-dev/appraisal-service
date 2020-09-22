package com.westernacher.internal.feedback.controller.representation;

import com.westernacher.internal.feedback.domain.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Data
public class MigrationAppraisalResponse {
    private List<AppraisalReview> appraisalReviews = new ArrayList<>();
    private List<AppraisalGoal> appraisalGoals = new ArrayList<>();
    private List<AppraisalRole> appraisalRoles = new ArrayList<>();
    private List<AppraisalReviewGoal> appraisalReviewGoals = new ArrayList<>();
    private List<Person> persons = new ArrayList<>();
    private Map<String, Integer> statistics = new HashMap<>();

    public MigrationAppraisalResponse() {
        this.statistics.put("appraisal.review", 0);
        this.statistics.put("appraisal.goal", 0);
        this.statistics.put("appraisal.role", 0);
        this.statistics.put("appraisal.review.goal", 0);
        this.statistics.put("person", 0);
    }

    public void addAppraisalReview(AppraisalReview appraisalReview) {
        appraisalReviews.add(appraisalReview);
        incrementStatistics("appraisal.review");
    }

    public void addAppraisalGoal(AppraisalGoal appraisalGoal) {
        appraisalGoals.add(appraisalGoal);
        incrementStatistics("appraisal.goal");
    }

    public void addAppraisalRole(AppraisalRole appraisalRole) {
        double totalScore = 0;
        boolean isComplete = false;
        for (AppraisalReviewGoal appraisalReviewGoal : appraisalReviewGoals) {
            if (appraisalReviewGoal.getEmployeeId().equals(appraisalRole.getEmployeeId())
                    && appraisalReviewGoal.getReviewerId().equals(appraisalRole.getReviewerId())
                    && appraisalReviewGoal.getReviewerType().equals(appraisalRole.getReviewerType())) {
                totalScore = totalScore + appraisalReviewGoal.getScore();
                isComplete = appraisalReviewGoal.isComplete() || isComplete;
            }
        }
        appraisalRole.setTotalScore(totalScore);
        appraisalRole.setComplete(isComplete);
        appraisalRoles.add(appraisalRole);
        incrementStatistics("appraisal.role");
    }

    public void addAppraisalReviewGoal(AppraisalReviewGoal appraisalReviewGoal) {
        appraisalReviewGoals.add(appraisalReviewGoal);
        incrementStatistics("appraisal.review.goal");
    }

    public void addPerson(Person person) {
        persons.add(person);
        incrementStatistics("person");
    }

    private void incrementStatistics(String key) {
        this.statistics.put(key, this.statistics.get(key) + 1);
    }

    public AppraisalGoal getAppraisalGoalBy(String job, String criteria) {
        AtomicReference<AppraisalGoal> matchingGoal = null;
        appraisalGoals.forEach(item -> {
            if (item.getJob().equals(job) && item.getCriteria().equals(criteria)) {
                matchingGoal.set(item);
            }
        });

        return matchingGoal == null ? null : matchingGoal.get();
    }
}
