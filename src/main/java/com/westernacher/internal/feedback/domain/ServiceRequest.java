package com.westernacher.internal.feedback.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@Document(collection = "service_request")
public class ServiceRequest {
    @Id
    private String id;
    private String userId;
    private String module;
    private String requestType;
    private String description;
    private Date createdDate;
    private ServiceRequestStatusType status;
    private List<ServiceRequestLog> log;

    public void addLog(ServiceRequestLog logEntry) {
        if (log == null) {
            log = new ArrayList<>();
        }
        log.add(logEntry);
    }
}
