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
    private String group;
    private String criteria;
    private String weightage;
    private List<Element> elements;
}

@Data
class Element {
    private String rating;
    private String description;
}
