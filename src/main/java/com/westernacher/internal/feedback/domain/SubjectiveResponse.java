package com.westernacher.internal.feedback.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubjectiveResponse {
    private String topic;
    private String duration;
    private String comment;
}
