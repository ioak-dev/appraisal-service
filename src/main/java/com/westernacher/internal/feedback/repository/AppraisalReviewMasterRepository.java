package com.westernacher.internal.feedback.repository;

import com.westernacher.internal.feedback.domain.AppraisalReview;
import com.westernacher.internal.feedback.domain.AppraisalReviewMaster;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AppraisalReviewMasterRepository extends MongoRepository<AppraisalReviewMaster, String> {

    List<AppraisalReviewMaster> findAllByAppraisalId(String appraisalId);
}
