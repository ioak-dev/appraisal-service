package com.westernacher.internal.feedback.repository;


import com.westernacher.internal.feedback.domain.AppraisalReviewGoal;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AppraisalReviewGoalRepository extends MongoRepository<AppraisalReviewGoal, String> {


}
