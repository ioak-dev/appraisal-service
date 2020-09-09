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
@Document(collection = "appraisal.goal")
public class AppraisalGoal {
    @Id
    private String id;
    private String jobName;
    private String group;
    private String criteria;
    private float weightage;
    private String description;
    private String cycleId;
}
