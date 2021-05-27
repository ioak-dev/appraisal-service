package com.westernacher.internal.feedback.repository.v1;

import com.westernacher.internal.feedback.domain.v1.GoalNew;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GoalRepositoryNew extends MongoRepository<GoalNew, String> {

}
