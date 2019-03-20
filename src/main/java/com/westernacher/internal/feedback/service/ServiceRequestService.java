package com.westernacher.internal.feedback.service;

import com.westernacher.internal.feedback.domain.ServiceRequest;
import com.westernacher.internal.feedback.domain.ServiceRequestLog;
import com.westernacher.internal.feedback.domain.ServiceRequestStatusType;
import com.westernacher.internal.feedback.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ServiceRequestService {

    @Autowired
    private ServiceRequestRepository repository;

    public void create(String userId, String module, String requestType, String description) {

        Date createdDate = new Date();

        ServiceRequestLog logEntry = ServiceRequestLog.builder()
                .userId(userId)
                .createdDate(createdDate)
                .comment(description)
                .build();

        ServiceRequest serviceRequest = ServiceRequest.builder()
                .userId(userId)
                .module(module)
                .requestType(requestType)
                .description(description)
                .createdDate(createdDate)
                .status(ServiceRequestStatusType.OPEN)
                .build();
        serviceRequest.addLog(logEntry);

        repository.save(serviceRequest);

    }

    public void addLog(ServiceRequestStatusType status, String id, String userId, String comment) {
        ServiceRequest request = repository.findById(id).orElse(null);

        request.addLog(ServiceRequestLog.builder()
                .userId(userId)
                .createdDate(new Date())
                .comment(comment)
                .build());

        request.setStatus(status);

        repository.save(request);
    }
}
