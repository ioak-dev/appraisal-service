package com.westernacher.internal.feedback.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Builder
public class ServiceRequestLog {
    private Date createdDate;
    private String comment;
    private String userId;
}
