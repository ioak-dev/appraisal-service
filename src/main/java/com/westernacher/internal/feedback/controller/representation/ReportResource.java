package com.westernacher.internal.feedback.controller.representation;

import com.westernacher.internal.feedback.domain.AppraisalStatusType;
import lombok.Data;

import java.util.List;

public class ReportResource {
    @Data
    public static class Summary {

        private String cycleId;
        private String cycleName;
        private String employeeId;
        private String employeeFirstName;
        private String employeeLastName;
        private String employeeEmail;
        private String job;
        private String cu;
        private String reviewerFirstName;
        private String reviewerLastName;
        private String reviewerEmail;
        private String reviewerType;
        private Double primaryScore;
        private Double secondaryScore;
        private String completionStatus;


    }
}
