package com.westernacher.internal.feedback.domain;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ReviewerElements {
    private String comment;
    private String rating;
    private String name;
    private boolean isComplete;
}
