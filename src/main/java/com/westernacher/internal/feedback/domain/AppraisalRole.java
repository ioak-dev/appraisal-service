package com.westernacher.internal.feedback.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "appraisal.role")
public class AppraisalRole {
    @Id
    private String id;
    private String reviewerId;
    private AppraisalStatusType reviewerType;
    private String employeeId;
    private String cycleId;
}
