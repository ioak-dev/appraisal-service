package com.westernacher.internal.feedback.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@Document(collection = "goal_definition")
public class GoalDefinition {
    @Id
    private String id;
    private JobName jobName;
    private String group;
    private String criteria;
    private int weightage;
    private String description;
}
