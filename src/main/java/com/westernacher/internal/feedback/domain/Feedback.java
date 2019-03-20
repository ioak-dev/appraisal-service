package com.westernacher.internal.feedback.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@Document(collection = "feedback")
public class Feedback {
    @Id
    private String id;
    private String cycleId;
    private String receiverId;
    private String providerId;
    private List<String> responses;
    private FeedbackStatusType status;
}
