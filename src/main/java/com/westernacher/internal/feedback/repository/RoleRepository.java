package com.westernacher.internal.feedback.repository;


import com.westernacher.internal.feedback.domain.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RoleRepository extends MongoRepository<Role, String> {

    List<Role> findByEmployeeId(String employeeId);
    List<Role> findByReviewerId(String reviewerId);

}
