package com.westernacher.internal.feedback.service;

import com.westernacher.internal.feedback.domain.AppraisalReviewGoal;
import com.westernacher.internal.feedback.domain.v2.AppraisalRole;
import com.westernacher.internal.feedback.domain.v2.Person;

import java.util.List;
import java.util.Map;

public interface AppraisalReviewGoalService {
    List<AppraisalReviewGoal> getReviewGoals (String appraisalId);

    List<AppraisalReviewGoal> update (List<AppraisalReviewGoal> reviewGoals);

    List<AppraisalReviewGoal> submit (List<AppraisalReviewGoal> reviewGoals);

    void sendMailAfterSubmit(List<AppraisalRole> appraisalRoleListForMail, Map<String, Person> personStore);

}
