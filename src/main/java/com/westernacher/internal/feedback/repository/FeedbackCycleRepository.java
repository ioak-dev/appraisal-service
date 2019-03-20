package com.westernacher.internal.feedback.repository;

import com.westernacher.internal.feedback.domain.FeedbackCycle;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FeedbackCycleRepository extends MongoRepository<FeedbackCycle, String> {
    List<FeedbackCycle> findAllByUnit(String unit);
}
