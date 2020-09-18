package com.westernacher.internal.feedback.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "goal")
public class Goal {
    @Id
    private String id;
    private String job;
    private String group;
    private String criteria;
    private float weightage;
    private String description;
    private int order;
    private String cu;
}
