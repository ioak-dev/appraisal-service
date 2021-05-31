package com.westernacher.internal.feedback.repository.v2;

import com.westernacher.internal.feedback.domain.v2.AppraisalDescriptive;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AppraisalDescriptiveRepository extends MongoRepository<AppraisalDescriptive, String> {

}
