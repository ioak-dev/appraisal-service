package com.westernacher.internal.feedback.repository;

import com.westernacher.internal.feedback.domain.AppraisalReview;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AppraisalReviewRepository extends MongoRepository<AppraisalReview, String> {
    /*List<Appraisal> findAllByCycleId(String cycleId);
    Appraisal findOneByCycleIdAndUserId(String cycleId, String userId);
    List<Appraisal> findAllByCycleIdAndStatus(String cycleId, String status);
    List<Appraisal> findAllByCycleIdAndUserIdIsIn(String cycleId, List<String> userIdList);



*/
}
