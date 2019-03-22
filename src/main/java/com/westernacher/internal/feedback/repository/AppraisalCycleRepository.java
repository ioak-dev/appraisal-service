package com.westernacher.internal.feedback.repository;

import com.westernacher.internal.feedback.domain.AppraisalCycle;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AppraisalCycleRepository extends MongoRepository<AppraisalCycle, String> {
    AppraisalCycle findByName(String name);
}
