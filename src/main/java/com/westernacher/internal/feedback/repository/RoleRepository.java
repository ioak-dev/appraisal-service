package com.westernacher.internal.feedback.repository;


import com.westernacher.internal.feedback.domain.AppraisalStatusType;
import com.westernacher.internal.feedback.domain.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Set;

public interface RoleRepository extends MongoRepository<Role, String> {

    List<Role> findByEmployeeId(String employeeId);

    List<Role> findByReviewerId(String reviewerId);

    List<Role> findAllByEmployeeIdIn(List<String> employeeIdList);

    Role findByEmployeeIdAndReviewerIdAndReviewerType(String employeeId, String reviewerId, AppraisalStatusType reviewerType);

    void deleteByEmployeeIdAndReviewerIdAndReviewerType(String employeeId, String reviewerId, AppraisalStatusType reviewerType);

    void deleteByEmployeeIdIn(Set<String> employeeId);


}
