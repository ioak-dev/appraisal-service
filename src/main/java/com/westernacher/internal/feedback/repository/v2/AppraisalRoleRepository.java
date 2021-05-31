package com.westernacher.internal.feedback.repository.v2;


import com.westernacher.internal.feedback.domain.v2.AppraisalRole;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AppraisalRoleRepository extends MongoRepository<AppraisalRole, String> {


}
