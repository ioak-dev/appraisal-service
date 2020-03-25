package com.westernacher.internal.feedback.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(collection = "rating_scale")
public class RatingScale {
    @Id
    private String id;
    private String rating;
    private String appraisalCriteria;
    private String reviewerCriteria;
}
