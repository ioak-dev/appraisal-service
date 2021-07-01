package com.westernacher.internal.feedback.repository;

import com.westernacher.internal.feedback.domain.v1Goal;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Set;

public interface v1GoalRepository extends MongoRepository<v1Goal, String> {

    void deleteAllByJobIn(Set<String> jobset);

    void deleteAllByCuIn(Set<String> cuset);

    List<v1Goal> findAllByJob(String job);
}
