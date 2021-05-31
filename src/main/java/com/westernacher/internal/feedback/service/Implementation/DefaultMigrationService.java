package com.westernacher.internal.feedback.service.Implementation;

import com.westernacher.internal.feedback.controller.representation.MigrationAppraisalResponse;
import com.westernacher.internal.feedback.domain.*;
import com.westernacher.internal.feedback.domain.v2.AppraisalRole;
import com.westernacher.internal.feedback.domain.v2.Person;
import com.westernacher.internal.feedback.repository.*;
import com.westernacher.internal.feedback.repository.v2.PersonRepository;
import com.westernacher.internal.feedback.service.MigrationService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DefaultMigrationService implements MigrationService {

    @Autowired
    private AppraisalReviewRepository appraisalReviewRepository;

    @Autowired
    private AppraisalGoalRepository appraisalGoalRepository;

    @Autowired
    private v1AppraisalRoleRepository v1AppraisalRoleRepository;

    @Autowired
    private AppraisalReviewGoalRepository appraisalReviewGoalRepository;

    @Autowired
    private AppraisalReviewMasterRepository appraisalReviewMasterRepository;

    @Autowired
    private PersonRepository personRepository;

    @Override
    public Map<String, MigrationAppraisalPerson> getPersonMap(List<MigrationAppraisalPerson> migrationAppraisalPeople) {
        Map<String, MigrationAppraisalPerson> personMap = new HashMap<>();
        for (MigrationAppraisalPerson person:migrationAppraisalPeople) {
            personMap.put(person.getId(), person);
        }
        return personMap;
    }

    @Override
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

    @Override
    public void migrateToNewDb(MigrationOutput output) {
        if (output.getAppraisalReviewMasters() != null && !output.getAppraisalReviewMasters().isEmpty()) {
            appraisalReviewMasterRepository.saveAll(output.getAppraisalReviewMasters());
        }

        if (output.getAppraisalReviews() != null && !output.getAppraisalReviews().isEmpty()) {
            appraisalReviewRepository.saveAll(output.getAppraisalReviews());
        }

        if (output.getAppraisalGoals() != null && !output.getAppraisalGoals().isEmpty()) {
            appraisalGoalRepository.saveAll(output.getAppraisalGoals());
        }

        if (output.getAppraisalRoles() != null && !output.getAppraisalRoles().isEmpty()) {
            v1AppraisalRoleRepository.saveAll(output.getAppraisalRoles());
        }

        if (output.getAppraisalReviewGoals() != null && !output.getAppraisalReviewGoals().isEmpty()) {
            appraisalReviewGoalRepository.saveAll(output.getAppraisalReviewGoals());
        }

        if (output.getPersons() != null && !output.getPersons().isEmpty()) {
            personRepository.saveAll(output.getPersons());
        }
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
        String nameParts[] = migrationAppraisalPerson.getName().split(" ");
        person.setFirstName(nameParts[0]);
        person.setLastName(nameParts[nameParts.length - 1]);
        person.setStatus(migrationAppraisalPerson.getStatus());
        person.setUnit(migrationAppraisalPerson.getUnit());
        response.addPerson(person);
    }

    private void migrateAppraisal(String cycleId, MigrationAppraisalResponse response, Map<String, MigrationAppraisalPerson> personMap, Map<String, Integer> goalOrder, MigrationAppraisal appraisal) {
        AppraisalReview appraisalReview = new AppraisalReview();
        appraisalReview.setId(new ObjectId().toString());
        appraisalReview.setCycleId(cycleId);
        appraisalReview.setEmployeeId(appraisal.getUserId());
        AppraisalStatusType status = AppraisalStatusType.Self;
        if (appraisal.getStatus().equals(AppraisalStatusType.SELF_APPRAISAL)) {
            status = AppraisalStatusType.Self;
        } else if (appraisal.getStatus().equals(AppraisalStatusType.PROJECT_MANAGER)) {
            status = AppraisalStatusType.Level_1;
        } else if (appraisal.getStatus().equals(AppraisalStatusType.REPORTING_MANAGER)) {
            status = AppraisalStatusType.Level_2;
        } else if (appraisal.getStatus().equals(AppraisalStatusType.PRACTICE_DIRECTOR)) {
            status = AppraisalStatusType.Level_3;
        } else if (appraisal.getStatus().equals(AppraisalStatusType.HR)) {
            status = AppraisalStatusType.Level_4;
        }

        appraisalReview.setStatus(status.name());
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
                populateAppraisalReviewGoals(AppraisalStatusType.Self.name(), response, appraisal.getUserId(), appraisalGoal, appraisalReview.getId(), selfReview);
                populateAppraisalReviewGoals(AppraisalStatusType.Level_1.name(), response, appraisal.getUserId(), appraisalGoal, appraisalReview.getId(), criteria.getProjectManagerReviews());
                populateAppraisalReviewGoals(AppraisalStatusType.Level_2.name(), response, appraisal.getUserId(), appraisalGoal, appraisalReview.getId(), criteria.getTeamLeadReviews());
                populateAppraisalReviewGoals(AppraisalStatusType.Level_3.name(), response, appraisal.getUserId(), appraisalGoal, appraisalReview.getId(), criteria.getPracticeDirectorReviews());
                populateAppraisalReviewGoals(AppraisalStatusType.Level_4.name(), response, appraisal.getUserId(), appraisalGoal, appraisalReview.getId(), criteria.getHrReviews());
                populateGoalSetting(response, appraisal.getUserId(), appraisalGoal, appraisalReview.getId(), criteria.getCustomDescription() == null ? "" : criteria.getCustomDescription());
            });
        });

        StringBuilder sectionTwoText = new StringBuilder();
        appraisal.getSectiontwoResponse().forEach(item -> {
            sectionTwoText.append(item.getTopic());
            sectionTwoText.append(": ");
            sectionTwoText.append(item.getComment());
            sectionTwoText.append("; ");
        });
        AppraisalGoal appraisalGoalSectionTwo = getAppraisalGoalCu(cycleId, response, personMap, 1, appraisal, "Notable contributions", "Additional activities/tasks performed");
        populateAppraisalReviewGoal(AppraisalStatusType.Self, response, appraisal.getUserId(), appraisalGoalSectionTwo, appraisalReview.getId(), sectionTwoText.toString());

        StringBuilder sectionThreeText = new StringBuilder();
        appraisal.getSectionthreeResponse().forEach(item -> {
            sectionThreeText.append(item.getTopic());
            sectionThreeText.append(": ");
            sectionThreeText.append(item.getComment());
            sectionThreeText.append("; ");
        });
        AppraisalGoal appraisalGoalSectionThree = getAppraisalGoalCu(cycleId, response, personMap, 1, appraisal, "Notable contributions", "Future aspirations");
        populateAppraisalReviewGoal(AppraisalStatusType.Self, response, appraisal.getUserId(), appraisalGoalSectionThree, appraisalReview.getId(), sectionThreeText.toString());

        //JSONObject jsonObject = new JSONObject();
        String sectionFourText ="";
        if (appraisal.getSectionfourResponse() != null) {
            try {
                JSONObject jsonObject = new JSONObject(appraisal.getSectionfourResponse());
                if (jsonObject.has("sectionfour")) {
                    sectionFourText = jsonObject.getString("sectionfour");
                }
            }catch (JSONException err){
                err.printStackTrace();
            }
        }


        AppraisalGoal appraisalGoalSectionFour = getAppraisalGoalCu(cycleId, response, personMap, 1, appraisal, "Notable contributions", "Additional feedback from you");
        populateAppraisalReviewGoal(AppraisalStatusType.Self, response, appraisal.getUserId(), appraisalGoalSectionFour, appraisalReview.getId(), sectionFourText);

        populateAppraisalRoles(cycleId, response, appraisal, appraisalReview);
    }

    private void populateAppraisalReviewGoals(String appraisalStatusType, MigrationAppraisalResponse response, String employeeId, AppraisalGoal appraisalGoal, String appraisalReviewId, Map<String, ReviewerElements> reviews) {
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
    
    private void populateGoalSetting(MigrationAppraisalResponse response, String employeeId, AppraisalGoal appraisalGoal, String appraisalReviewId, String comment) {
        AppraisalReviewGoal setGoal = new AppraisalReviewGoal();
        setGoal.setId(new ObjectId().toString());
        setGoal.setAppraisalId(appraisalReviewId);
        setGoal.setEmployeeId(employeeId);
        setGoal.setReviewerType(AppraisalStatusType.SET_GOAL.name());
        setGoal.setComment(comment);
        setGoal.setGoalId(appraisalGoal.getId());
        response.addAppraisalReviewGoal(setGoal);

//        AppraisalReviewGoal reviewGoal = new AppraisalReviewGoal();
//        reviewGoal.setId(new ObjectId().toString());
//        reviewGoal.setAppraisalId(appraisalReviewId);
//        reviewGoal.setEmployeeId(employeeId);
//        reviewGoal.setReviewerType(AppraisalStatusType.REVIEW_GOAL);
//        reviewGoal.setComment("");
//        reviewGoal.setGoalId(appraisalGoal.getId());
//        response.addAppraisalReviewGoal(reviewGoal);
    }

    private void populateAppraisalReviewGoal(AppraisalStatusType appraisalStatusType, MigrationAppraisalResponse response, String employeeId, AppraisalGoal appraisalGoal, String appraisalReviewId, String comment) {
        AppraisalReviewGoal appraisalReviewGoal = new AppraisalReviewGoal();
        appraisalReviewGoal.setId(new ObjectId().toString());
        appraisalReviewGoal.setAppraisalId(appraisalReviewId);
        appraisalReviewGoal.setEmployeeId(employeeId);
        appraisalReviewGoal.setReviewerId(employeeId);
        appraisalReviewGoal.setReviewerType(appraisalStatusType.name());
        appraisalReviewGoal.setComment(comment);
        // appraisalReviewGoal.setRating();
        appraisalReviewGoal.setGoalId(appraisalGoal.getId());
        appraisalReviewGoal.setComplete(true);
        appraisalReviewGoal.setScore(0);
        response.addAppraisalReviewGoal(appraisalReviewGoal);
    }

    private void populateAppraisalRoles(String cycleId, MigrationAppraisalResponse response, MigrationAppraisal appraisal, AppraisalReview appraisalReview) {
        response.addAppraisalRole(getAppraisalRole(AppraisalStatusType.Self, cycleId, appraisal.getUserId(), appraisal.getUserId()));
        ObjectiveResponse reviewerElements = appraisal.getSectiononeResponse().get(0).getResponse().get(0);
        Map<String, ReviewerElements> pmReviews = reviewerElements.getProjectManagerReviews();
        pmReviews.keySet().forEach(reviewerId -> {
            response.addAppraisalRole(getAppraisalRole(AppraisalStatusType.Level_1, cycleId, appraisal.getUserId(), reviewerId));
        });
        Map<String, ReviewerElements> pdReviews = reviewerElements.getPracticeDirectorReviews();
        pdReviews.keySet().forEach(reviewerId -> {
            response.addAppraisalRole(getAppraisalRole(AppraisalStatusType.Level_3, cycleId, appraisal.getUserId(), reviewerId));
        });
        Map<String, ReviewerElements> hrReviews = reviewerElements.getHrReviews();
        hrReviews.keySet().forEach(reviewerId -> {
            response.addAppraisalRole(getAppraisalRole(AppraisalStatusType.Level_4, cycleId, appraisal.getUserId(), reviewerId));
            response.addAppraisalRole(getAppraisalRole(AppraisalStatusType.Master, cycleId, appraisal.getUserId(), reviewerId));
            response.addAppraisalReviewMaster(getAppraisalReviewMaster(cycleId, appraisal.getUserId(), reviewerId, appraisalReview.getId()));
        });
        Map<String, ReviewerElements> tlReviews = reviewerElements.getTeamLeadReviews();
        tlReviews.keySet().forEach(reviewerId -> {
            response.addAppraisalRole(getAppraisalRole(AppraisalStatusType.Level_2, cycleId, appraisal.getUserId(), reviewerId));
        });
    }

    private AppraisalRole getAppraisalRole(AppraisalStatusType reviewerType, String cycleId, String employeeId, String reviewerId) {
        AppraisalRole appraisalRole = new AppraisalRole();
        appraisalRole.setId(new ObjectId().toString());
        appraisalRole.setCycleId(cycleId);
        appraisalRole.setEmployeeId(employeeId);
        appraisalRole.setReviewerId(reviewerId);
        appraisalRole.setReviewerType(reviewerType.name());
        return appraisalRole;
    }

    private AppraisalReviewMaster getAppraisalReviewMaster(String cycleId, String employeeId, String reviewerId, String appraisalId) {
        AppraisalReviewMaster appraisalReviewMaster = new AppraisalReviewMaster();
        appraisalReviewMaster.setId(new ObjectId().toString());
        appraisalReviewMaster.setAppraisalId(appraisalId);
        appraisalReviewMaster.setComment("");
        appraisalReviewMaster.setComplete(false);
        appraisalReviewMaster.setEmployeeId(employeeId);
        appraisalReviewMaster.setRating("");
        appraisalReviewMaster.setReviewerId(reviewerId);
        return appraisalReviewMaster;
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
        appraisalGoal.setOrder(goalOrder.get(criteria.getCriteria().trim()));

        response.addAppraisalGoal(appraisalGoal);
        return appraisalGoal;
    }

    private AppraisalGoal getAppraisalGoalCu(String cycleId, MigrationAppraisalResponse response, Map<String, MigrationAppraisalPerson> personMap, int goalOrder, MigrationAppraisal appraisal, String group, String criteria) {
        AppraisalGoal appraisalGoal = response.getAppraisalGoalByCu("WIN", criteria);
        if (appraisalGoal != null) {
            return appraisalGoal;
        }
        appraisalGoal = new AppraisalGoal();
        appraisalGoal.setId(new ObjectId().toString());
        appraisalGoal.setGroup(group);
        appraisalGoal.setCriteria(criteria);
        appraisalGoal.setDescription(" ");
        appraisalGoal.setWeightage(Float.valueOf(0));
        appraisalGoal.setCycleId(cycleId);
        appraisalGoal.setCu("WIN");
        appraisalGoal.setOrder(goalOrder);

        response.addAppraisalGoal(appraisalGoal);
        return appraisalGoal;
    }
}
