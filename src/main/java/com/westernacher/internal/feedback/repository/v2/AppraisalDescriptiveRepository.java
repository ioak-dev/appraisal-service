package com.westernacher.internal.feedback.repository.v2;

import com.westernacher.internal.feedback.domain.v2.AppraisalDescriptive;
import com.westernacher.internal.feedback.domain.v2.AppraisalLong;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AppraisalDescriptiveRepository extends MongoRepository<AppraisalDescriptive, String> {

    List<AppraisalDescriptive> findAllByHeaderId(String headerId);

    List<AppraisalDescriptive> findAllByHeaderIdIn(List<String> headerIds);

}
