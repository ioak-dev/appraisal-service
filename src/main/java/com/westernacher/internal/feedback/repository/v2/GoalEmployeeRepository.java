package com.westernacher.internal.feedback.repository.v2;

import com.westernacher.internal.feedback.domain.v2.GoalEmployee;
import com.westernacher.internal.feedback.domain.v2.GoalReference;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GoalEmployeeRepository extends MongoRepository<GoalEmployee, String> {
    List<GoalEmployee> findAllByEmployeeId(String employeeId);

}
