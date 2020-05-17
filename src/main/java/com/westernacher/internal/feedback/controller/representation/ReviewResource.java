package com.westernacher.internal.feedback.controller.representation;

import com.westernacher.internal.feedback.domain.RoleType;
import lombok.Data;

@Data
public class ReviewResource {
    private RoleType roleType;
    private String group;
    private String criteria;
    private String reviewerId;
    private String rating;
    private String comment;
    private float customWeightage;
    private String customDescription;
}
