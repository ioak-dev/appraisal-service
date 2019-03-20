package com.westernacher.internal.feedback.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Document(collection = "appraisal_cycle")
public class AppraisalCycle {

    @Id
    private String id;
    private String name;
    private String evaluationPeriod;
    private List<CriteriaGroup> sectiononeCriteria;
    private AppraisalCycleStatusType status;
    private Date startDate;
    private Date selfAppraisalDeadline;

}
