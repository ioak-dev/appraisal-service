package com.westernacher.internal.feedback.repository.v2;

import com.westernacher.internal.feedback.domain.v2.Goal;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GoalRepository extends MongoRepository<Goal, String> {

}
