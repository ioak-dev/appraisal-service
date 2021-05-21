package com.westernacher.internal.feedback.repository;


import com.westernacher.internal.feedback.domain.AppraisalReviewGoal;
import com.westernacher.internal.feedback.domain.AppraisalStatusType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Set;

public interface AppraisalReviewGoalRepository extends MongoRepository<AppraisalReviewGoal, String> {

    List<AppraisalReviewGoal> findAllByAppraisalId(String appraisalId);

    List<AppraisalReviewGoal> findAllByAppraisalIdAndReviewerId(String appraisalId, String reviewerId);

    List<AppraisalReviewGoal> findByGoalId(String goalId);

    List<AppraisalReviewGoal> findAllByAppraisalIdInAndReviewerType(List appraisalIds, AppraisalStatusType type);

    long deleteAllByAppraisalIdInAndReviewerType(Set appraisalIds, AppraisalStatusType type);

    long deleteAllByAppraisalIdIn(List<String> appraisalIds);

    List<AppraisalReviewGoal> findAllByEmployeeId(String employeeId);

    List<AppraisalReviewGoal> findAllByGoalIdAndEmployeeId(String goalId, String employeeId);

    List<AppraisalReviewGoal> findAllByAppraisalIdAndReviewerIdAndReviewerType(String appraisalId, String reviewerId, String reviewerType);


}
