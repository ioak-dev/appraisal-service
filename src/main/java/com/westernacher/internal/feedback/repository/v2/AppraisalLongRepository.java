package com.westernacher.internal.feedback.repository.v2;

import com.westernacher.internal.feedback.domain.v2.AppraisalLong;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AppraisalLongRepository extends MongoRepository<AppraisalLong, String> {

    List<AppraisalLong> findAllByHeaderId(String headerId);

}
