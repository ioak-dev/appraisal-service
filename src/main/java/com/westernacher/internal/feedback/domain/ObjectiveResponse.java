package com.westernacher.internal.feedback.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ObjectiveResponse {
    private String criteria;
    private int weightage;
    private String selfComment;
    private String selfRating;
    private String reviewerComment;
    private String reviewerRating;
}
