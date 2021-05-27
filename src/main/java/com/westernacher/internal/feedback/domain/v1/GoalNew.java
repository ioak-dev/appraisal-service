package com.westernacher.internal.feedback.domain.v1;

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
@Document(collection = "goal")
public class GoalNew {
    @Id
    private String id;
    private int order;
    private String group;
    private String criteria;

}
