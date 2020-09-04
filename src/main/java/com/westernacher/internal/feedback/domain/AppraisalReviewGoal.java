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
@Document(collection = "appraisal.review.goal")
public class AppraisalReviewGoal {
    @Id
    private String id;
    private String employeeId;
    private String reviewerId;
    private RoleType reviewerType;
    private String goalDefinitionId;
    private String comment;
    private String rating;
}
