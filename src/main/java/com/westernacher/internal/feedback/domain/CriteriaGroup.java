package com.westernacher.internal.feedback.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CriteriaGroup {
    private String group;
    private List<Criteria> criterias;
}

