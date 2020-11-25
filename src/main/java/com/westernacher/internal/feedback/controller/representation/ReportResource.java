package com.westernacher.internal.feedback.controller.representation;

import com.westernacher.internal.feedback.domain.AppraisalStatusType;
import lombok.Data;

import java.util.List;

public class ReportResource {
    @Data
    public static class Summary {

        private String cycleId;
        private String employeeName;
        private String employeeId;
        private String employeeEmail;
        private String job;
        private String cu;
        private String reviewerName;
        private String reviewerEmail;
        private AppraisalStatusType reviewerType;
        private Double primaryScore;
        private Double secondaryScore;
        private Boolean isComplete;


    }
}
