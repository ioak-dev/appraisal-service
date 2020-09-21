package com.westernacher.internal.feedback.service.Implementation;

import com.westernacher.internal.feedback.controller.representation.MigrationAppraisalResponse;
import com.westernacher.internal.feedback.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MigrationService {

    public Map<String, MigrationAppraisalPerson> getPersonMap(List<MigrationAppraisalPerson> migrationAppraisalPeople) {
        Map<String, MigrationAppraisalPerson> personMap = new HashMap<>();
        for (MigrationAppraisalPerson person:migrationAppraisalPeople) {
            personMap.put(person.getId(), person);
        }
        return personMap;
    }

    public MigrationAppraisalResponse migrate(String cycleId, List<MigrationAppraisal> appraisalList, Map<String, MigrationAppraisalPerson> personMap, Map<String, Integer> goalOrder) {
        MigrationAppraisalResponse response = new MigrationAppraisalResponse();
        personMap.values().forEach(person -> {
            migratePerson(response, person);
        });
        appraisalList.forEach(appraisal -> {
            migrateAppraisal(cycleId, response, personMap, goalOrder, appraisal);
        });
        return response;
    }

    private void migratePerson(MigrationAppraisalResponse response, MigrationAppraisalPerson migrationAppraisalPerson) {
        Person person = new Person();
        person.setCu("WIN");
        person.setEmail(migrationAppraisalPerson.getEmail());
        person.setEmpId(migrationAppraisalPerson.getEmpId());
        person.setId(migrationAppraisalPerson.getId());
        person.setJob(migrationAppraisalPerson.getJobName());
        person.setJoiningDate(migrationAppraisalPerson.getJoiningDate());
        person.setLastAppraisalDate(migrationAppraisalPerson.getLastAppraisalDate());
        person.setLevel(migrationAppraisalPerson.getLevel());
        person.setName(migrationAppraisalPerson.getName());
        person.setSpecialization(migrationAppraisalPerson.getSpecialization());
        person.setStatus(migrationAppraisalPerson.getStatus());
        person.setUnit(migrationAppraisalPerson.getUnit());
        response.addPerson(person);
    }

    private void migrateAppraisal(String cycleId, MigrationAppraisalResponse response, Map<String, MigrationAppraisalPerson> personMap, Map<String, Integer> goalOrder, MigrationAppraisal appraisal) {
        AppraisalReview appraisalReview = new AppraisalReview();
        appraisalReview.setId(new ObjectId().toString());
        appraisalReview.setCycleId(cycleId);
        appraisalReview.setEmployeeId(appraisal.getUserId());
        appraisalReview.setStatus(appraisal.getStatus());
        response.addAppraisalReview(appraisalReview);
        appraisal.getSectiononeResponse().forEach(group -> {
            group.getResponse().forEach(criteria -> {
                AppraisalGoal appraisalGoal = getAppraisalGoal(cycleId,response, personMap, goalOrder, appraisal, group, criteria);
                Map<String, ReviewerElements> selfReview = new HashMap<>();
                selfReview.put(appraisal.getUserId(), ReviewerElements.builder()
                        .comment(criteria.getSelfComment())
                        .rating(criteria.getSelfRating())
                        .isComplete(!appraisal.getStatus().equals(AppraisalStatusType.SELF_APPRAISAL) && !appraisal.getStatus().equals(AppraisalStatusType.SET_GOALS))
                        .build()
                );
                populateAppraisalReviewGoals(AppraisalStatusType.SELF_APPRAISAL, response, appraisal.getUserId(), appraisalGoal, appraisalReview.getId(), selfReview);
                populateAppraisalReviewGoals(AppraisalStatusType.PROJECT_MANAGER, response, appraisal.getUserId(), appraisalGoal, appraisalReview.getId(), criteria.getProjectManagerReviews());
                populateAppraisalReviewGoals(AppraisalStatusType.REPORTING_MANAGER, response, appraisal.getUserId(), appraisalGoal, appraisalReview.getId(), criteria.getTeamLeadReviews());
                populateAppraisalReviewGoals(AppraisalStatusType.PRACTICE_DIRECTOR, response, appraisal.getUserId(), appraisalGoal, appraisalReview.getId(), criteria.getPracticeDirectorReviews());
                populateAppraisalReviewGoals(AppraisalStatusType.HR, response, appraisal.getUserId(), appraisalGoal, appraisalReview.getId(), criteria.getHrReviews());
            });
        });
        populateAppraisalRoles(cycleId,response, appraisal);
    }

    private void populateAppraisalReviewGoals(AppraisalStatusType appraisalStatusType, MigrationAppraisalResponse response, String employeeId, AppraisalGoal appraisalGoal, String appraisalReviewId, Map<String, ReviewerElements> reviews) {
        for (String reviewerId : reviews.keySet()) {
            ReviewerElements reviewerElements = reviews.get(reviewerId);
            AppraisalReviewGoal appraisalReviewGoal = new AppraisalReviewGoal();
            appraisalReviewGoal.setId(new ObjectId().toString());
            appraisalReviewGoal.setAppraisalId(appraisalReviewId);
            appraisalReviewGoal.setEmployeeId(employeeId);
            appraisalReviewGoal.setReviewerId(reviewerId);
            appraisalReviewGoal.setReviewerType(appraisalStatusType);
            appraisalReviewGoal.setComment(reviewerElements.getComment());
            appraisalReviewGoal.setRating(reviewerElements.getRating());
            appraisalReviewGoal.setGoalId(appraisalGoal.getId());
            appraisalReviewGoal.setComplete(reviewerElements.isComplete());
            if (appraisalReviewGoal.getRating() != null && appraisalReviewGoal.getRating().length() > 0) {
                double weightage = appraisalGoal.getWeightage();
                int rating = Integer.parseInt(appraisalReviewGoal.getRating().substring(0,1));
                appraisalReviewGoal.setScore(weightage * rating);
            }
            response.addAppraisalReviewGoal(appraisalReviewGoal);
        }
    }

    private void populateAppraisalRoles(String cycleId, MigrationAppraisalResponse response, MigrationAppraisal appraisal) {
        response.addAppraisalRole(getAppraisalRole(AppraisalStatusType.SELF_APPRAISAL, cycleId, appraisal.getUserId(), appraisal.getUserId()));
        ObjectiveResponse reviewerElements = appraisal.getSectiononeResponse().get(0).getResponse().get(0);
        Map<String, ReviewerElements> pmReviews = reviewerElements.getProjectManagerReviews();
        pmReviews.keySet().forEach(reviewerId -> {
            response.addAppraisalRole(getAppraisalRole(AppraisalStatusType.PROJECT_MANAGER, cycleId, appraisal.getUserId(), reviewerId));
        });
        Map<String, ReviewerElements> pdReviews = reviewerElements.getPracticeDirectorReviews();
        pdReviews.keySet().forEach(reviewerId -> {
            response.addAppraisalRole(getAppraisalRole(AppraisalStatusType.PRACTICE_DIRECTOR, cycleId, appraisal.getUserId(), reviewerId));
        });
        Map<String, ReviewerElements> hrReviews = reviewerElements.getHrReviews();
        hrReviews.keySet().forEach(reviewerId -> {
            response.addAppraisalRole(getAppraisalRole(AppraisalStatusType.HR, cycleId, appraisal.getUserId(), reviewerId));
        });
        Map<String, ReviewerElements> tlReviews = reviewerElements.getTeamLeadReviews();
        tlReviews.keySet().forEach(reviewerId -> {
            response.addAppraisalRole(getAppraisalRole(AppraisalStatusType.REPORTING_MANAGER, cycleId, appraisal.getUserId(), reviewerId));
        });
    }

    private AppraisalRole getAppraisalRole(AppraisalStatusType reviewerType, String cycleId, String employeeId, String reviewerId) {
        AppraisalRole appraisalRole = new AppraisalRole();
        appraisalRole.setId(new ObjectId().toString());
        appraisalRole.setCycleId(cycleId);
        appraisalRole.setEmployeeId(employeeId);
        appraisalRole.setReviewerId(reviewerId);
        appraisalRole.setReviewerType(reviewerType);
        return appraisalRole;
    }

    private AppraisalGoal getAppraisalGoal(String cycleId, MigrationAppraisalResponse response, Map<String, MigrationAppraisalPerson> personMap, Map<String, Integer> goalOrder, MigrationAppraisal appraisal, ObjectiveResponseGroup group, ObjectiveResponse criteria) {
        AppraisalGoal appraisalGoal = response.getAppraisalGoalBy(personMap.get(appraisal.getUserId()).getJobName(), criteria.getCriteria());
        if (appraisalGoal != null) {
            return appraisalGoal;
        }
        appraisalGoal = new AppraisalGoal();
        appraisalGoal.setId(new ObjectId().toString());
        appraisalGoal.setGroup(group.getGroup());
        appraisalGoal.setCriteria(criteria.getCriteria());
        appraisalGoal.setDescription(criteria.getDescription());
        appraisalGoal.setWeightage(criteria.getWeightage());
        appraisalGoal.setCycleId(cycleId);
        appraisalGoal.setJob(personMap.get(appraisal.getUserId()).getJobName());
        appraisalGoal.setOrder(goalOrder.get(criteria.getCriteria()));
        appraisalGoal.setCu("WIN");

        response.addAppraisalGoal(appraisalGoal);
        return appraisalGoal;
    }
}
