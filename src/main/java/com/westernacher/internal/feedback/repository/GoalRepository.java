package com.westernacher.internal.feedback.repository;

import com.westernacher.internal.feedback.domain.Goal;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Set;

public interface GoalRepository extends MongoRepository<Goal, String> {

    void deleteAllByJobIn(Set<String> jobset);
    void deleteAllByCuIn(Set<String> cuset);
}
