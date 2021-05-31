package com.westernacher.internal.feedback.repository.v2;

import com.westernacher.internal.feedback.domain.v2.Goal;
import com.westernacher.internal.feedback.domain.v2.GoalReference;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GoalReferenceRepository extends MongoRepository<GoalReference, String> {

}
