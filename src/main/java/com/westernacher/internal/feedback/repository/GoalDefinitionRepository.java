package com.westernacher.internal.feedback.repository;

import com.westernacher.internal.feedback.domain.GoalDefinition;
import com.westernacher.internal.feedback.domain.JobName;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GoalDefinitionRepository extends MongoRepository<GoalDefinition, String> {
    List<GoalDefinition> getAllByJobName(String jobName);
}
