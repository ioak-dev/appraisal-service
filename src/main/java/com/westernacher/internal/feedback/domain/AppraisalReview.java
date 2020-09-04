package com.westernacher.internal.feedback.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(collection = "appraisal.review")
public class AppraisalReview {
    @Id
    private String id;
    private String reviewerId;
    private RoleType reviewerType;
    private AppraisalStatusType status;
}
