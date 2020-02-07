package com.westernacher.internal.feedback.controller.representation;

import lombok.Data;

@Data
public class ReviewResource {
    private String group;
    private String criteria;
    private String reviewerId;
    private String rating;
    private String comment;
}
