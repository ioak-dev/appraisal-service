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

    @Encrypted
    private String name;

    @Encrypted
    private AppraisalCycleStatusType status;
    private String cu;

    @Encrypted
    private Map<AppraisalStatusType, Date> deadline;

    @Encrypted
    private Map<AppraisalStatusType, String> workflowMap;

    @Encrypted
    private Map<String, List<String>> visibilityMap;

    @Encrypted
    private Boolean showReviewToSelf;

    @Encrypted
    private Integer minCommentLength;

    @Encrypted
    private Date start;

    @Encrypted
    private Date end;

}

