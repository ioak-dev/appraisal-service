package com.westernacher.internal.feedback.domain;

import com.bol.secure.Encrypted;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "appraisal.cycle")
public class AppraisalCycle {

    @Id
    private String id;

    private String name;

    private AppraisalCycleStatusType status;
    private String cu;

    private Map<AppraisalStatusType, Date> deadline;

    private Map<AppraisalStatusType, String> workflowMap;

    private Map<String, List<String>> visibilityMap;

    private Boolean showReviewToSelf;

    private Integer minCommentLength;

    private Date start;

    private Date end;

    private String admin;

}

