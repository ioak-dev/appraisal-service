package com.westernacher.internal.feedback.domain;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ObjectiveResponse {
    private String criteria;
    private Float weightage;
    private String description;
    private Float customWeightage;
    private String customDescription;
    private String selfComment;
    private String selfRating;
    private Map<String, ReviewerElements> projectManagerReviews;
    private Map<String, ReviewerElements> teamLeadReviews;
    private Map<String, ReviewerElements> practiceDirectorReviews;
    private Map<String, ReviewerElements> hrReviews;

}

