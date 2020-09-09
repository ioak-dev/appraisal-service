package com.westernacher.internal.feedback.repository;


import com.westernacher.internal.feedback.domain.AppraisalGoal;
import com.westernacher.internal.feedback.domain.AppraisalRole;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AppraisalRoleRepository extends MongoRepository<AppraisalRole, String> {

    List<AppraisalRole> findAllByCycleId(String cycleId);


}
