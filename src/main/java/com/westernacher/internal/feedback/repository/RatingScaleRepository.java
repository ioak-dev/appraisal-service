package com.westernacher.internal.feedback.repository;

import com.westernacher.internal.feedback.domain.GoalDefinition;
import com.westernacher.internal.feedback.domain.RatingScale;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RatingScaleRepository extends MongoRepository<RatingScale, String> {
}
