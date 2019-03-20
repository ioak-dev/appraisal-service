package com.westernacher.internal.feedback.repository;

import com.westernacher.internal.feedback.domain.GoalDefinition;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GoalDefinitionRepository extends MongoRepository<GoalDefinition, String> {
}
