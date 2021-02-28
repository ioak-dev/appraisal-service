package com.westernacher.internal.feedback.repository;

import com.westernacher.internal.feedback.domain.AppraisalReview;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AppraisalReviewRepository extends MongoRepository<AppraisalReview, String> {

    List<AppraisalReview> findAllByCycleId(String cycleId);

    List<AppraisalReview> findAllByCycleIdAndStatus(String cycleId, String status);

    List<AppraisalReview> findAllByCycleIdAndEmployeeIdIn(String cycleId, List<String> employeeIds);

    long deleteAllByCycleId(String cycleId);

    AppraisalReview findFirstByCycleIdAndEmployeeId(String cycleId, String employeeId);

}
