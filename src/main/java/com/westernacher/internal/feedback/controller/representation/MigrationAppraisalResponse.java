package com.westernacher.internal.feedback.controller.representation;

import com.westernacher.internal.feedback.domain.*;
import com.westernacher.internal.feedback.domain.v2.AppraisalRole;
import com.westernacher.internal.feedback.domain.v2.Person;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class MigrationAppraisalResponse {
    private List<AppraisalReview> appraisalReviews = new ArrayList<>();
    private List<AppraisalGoal> appraisalGoals = new ArrayList<>();
    private List<AppraisalRole> appraisalRoles = new ArrayList<>();
    private List<AppraisalReviewGoal> appraisalReviewGoals = new ArrayList<>();
    private List<Person> persons = new ArrayList<>();
    private List<AppraisalReviewMaster> appraisalReviewMasters = new ArrayList<>();
    private Map<String, Integer> statistics = new HashMap<>();

    public MigrationAppraisalResponse() {
        this.statistics.put("appraisal.review", 0);
        this.statistics.put("appraisal.goal", 0);
        this.statistics.put("appraisal.role", 0);
        this.statistics.put("appraisal.review.goal", 0);
        this.statistics.put("appraisal.review.master", 0);
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
                    && appraisalReviewGoal.getReviewerId() != null && appraisalReviewGoal.getReviewerId().equals(appraisalRole.getReviewerId())
                    && appraisalReviewGoal.getReviewerType().equals(appraisalRole.getReviewerType())) {
                totalScore = totalScore + appraisalReviewGoal.getScore();
                isComplete = appraisalReviewGoal.isComplete() || isComplete;
            }
        }
        appraisalRole.setPrimaryScore(totalScore);
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

    public void addAppraisalReviewMaster(AppraisalReviewMaster appraisalReviewMaster) {
        appraisalReviewMasters.add(appraisalReviewMaster);
        incrementStatistics("appraisal.review.master");
    }

    private void incrementStatistics(String key) {
        this.statistics.put(key, this.statistics.get(key) + 1);
    }

    public AppraisalGoal getAppraisalGoalBy(String job, String criteria) {
        AppraisalGoal matchingGoal = null;
        for (AppraisalGoal appraisalGoal : appraisalGoals) {

            if (appraisalGoal.getJob() != null && appraisalGoal.getJob().equals(job) && appraisalGoal.getCriteria().equals(criteria)) {
                matchingGoal = appraisalGoal;
            }
        }

        return matchingGoal;
    }

    public AppraisalGoal getAppraisalGoalByCu(String cu, String criteria) {
        AppraisalGoal matchingGoal = null;
        for (AppraisalGoal appraisalGoal : appraisalGoals) {

            if (appraisalGoal.getCu() != null && appraisalGoal.getCu().equals(cu) && appraisalGoal.getCriteria().equals(criteria)) {
                matchingGoal = appraisalGoal;
            }
        }

        return matchingGoal;
    }
}
