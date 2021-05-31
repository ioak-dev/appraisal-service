package com.westernacher.internal.feedback.repository.v2;

import com.westernacher.internal.feedback.domain.v2.GoalEmployee;
import com.westernacher.internal.feedback.domain.v2.GoalReference;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GoalEmployeeRepository extends MongoRepository<GoalEmployee, String> {

}
