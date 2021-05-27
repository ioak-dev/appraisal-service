package com.westernacher.internal.feedback.repository.v1;

import com.westernacher.internal.feedback.domain.v1.AppraisalObjective;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AppraisalObjectiveRepositoryNew extends MongoRepository<AppraisalObjective, String> {

}
