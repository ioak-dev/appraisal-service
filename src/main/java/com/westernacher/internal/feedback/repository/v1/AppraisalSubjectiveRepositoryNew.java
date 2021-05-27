package com.westernacher.internal.feedback.repository.v1;

import com.westernacher.internal.feedback.domain.v1.AppraisalSubjective;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AppraisalSubjectiveRepositoryNew extends MongoRepository<AppraisalSubjective, String> {

}
