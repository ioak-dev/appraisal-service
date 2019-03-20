package com.westernacher.internal.feedback.domain;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Document(collection = "feedback_cycle")
public class FeedbackCycle {

    @Id
    private String id;
    private String unit;
    private String name;
    private Date startDate;
    private Date endDate;
    private FeedbackCycleStatusType status;
    private List<String> questions;

}
