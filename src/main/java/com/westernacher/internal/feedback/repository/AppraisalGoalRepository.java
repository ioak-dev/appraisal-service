package com.westernacher.internal.feedback.repository;


import com.westernacher.internal.feedback.domain.AppraisalGoal;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AppraisalGoalRepository extends MongoRepository<AppraisalGoal, String> {

    List<AppraisalGoal> findAllByCycleId(String cycleId);
    List<AppraisalGoal> findAllByCuIsNull();
    List<AppraisalGoal> findAllByCuIs(String cu);
    List<AppraisalGoal> findAllByJob(String job);

    long deleteAllByCycleId(String cycleId);
}
