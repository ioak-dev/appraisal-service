package com.westernacher.internal.feedback.repository;

import com.westernacher.internal.feedback.domain.ServiceRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ServiceRequestRepository extends MongoRepository<ServiceRequest, String> {

    List<ServiceRequest> findAllByUserId(String userId);

}
