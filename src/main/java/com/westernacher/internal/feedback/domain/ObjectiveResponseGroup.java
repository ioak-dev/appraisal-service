package com.westernacher.internal.feedback.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ObjectiveResponseGroup {
    private String group;
    private List<ObjectiveResponse> response;
}

