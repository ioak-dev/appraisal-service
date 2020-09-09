package com.westernacher.internal.feedback.repository;

import com.westernacher.internal.feedback.domain.AppraisalReview;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AppraisalReviewRepository extends MongoRepository<AppraisalReview, String> {

    List<AppraisalReview> findAllByCycleId(String cycleId);

}
