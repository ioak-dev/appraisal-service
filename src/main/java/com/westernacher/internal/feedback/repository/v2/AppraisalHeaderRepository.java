package com.westernacher.internal.feedback.repository.v2;

import com.westernacher.internal.feedback.domain.v2.AppraisalDescriptive;
import com.westernacher.internal.feedback.domain.v2.AppraisalHeader;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AppraisalHeaderRepository extends MongoRepository<AppraisalHeader, String> {

}
