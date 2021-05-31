package com.westernacher.internal.feedback.domain.v2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "goal.reference")
public class GoalReference {
    @Id
    private String id;
    private String goalOrder;
    private float weightage;
    private String job;
    private String description;
}
