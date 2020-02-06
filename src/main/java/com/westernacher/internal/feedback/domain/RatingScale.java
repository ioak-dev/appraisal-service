package com.westernacher.internal.feedback.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@Document(collection = "rating_scale")
public class RatingScale {
    @Id
    private String id;
    private String group;
    private String criteria;
    private String weightage;
    private List<ElementScale> elements;
}

@Data
class ElementScale {
    private String rating;
    private String description;
}
