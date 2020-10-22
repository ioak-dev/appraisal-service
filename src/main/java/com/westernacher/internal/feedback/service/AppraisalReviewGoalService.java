package com.westernacher.internal.feedback.service;

import com.westernacher.internal.feedback.domain.AppraisalReviewGoal;
import com.westernacher.internal.feedback.domain.AppraisalRole;
import com.westernacher.internal.feedback.domain.Goal;
import com.westernacher.internal.feedback.domain.Person;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface AppraisalReviewGoalService {
    List<AppraisalReviewGoal> getReviewGoals (String appraisalId);

    List<AppraisalReviewGoal> update (List<AppraisalReviewGoal> reviewGoals);

    List<AppraisalReviewGoal> submit (List<AppraisalReviewGoal> reviewGoals);

    void sendMailAfterSubmit(List<AppraisalRole> appraisalRoleListForMail, Map<String, Person> personStore);

}
