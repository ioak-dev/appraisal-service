package com.westernacher.internal.feedback.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

public class Report {

    @Data
    public static class ReportDetails {
        String groupName;
        private List<CriteriaDetails> criteriaDetails;
    }

    @Data
    public static class CriteriaDetails {
        private String groupName;
        private String criteriaName;
        private String criteriaDescription;
        private String weightage;
        private String reviewGoal;
        private String setGoal;
        private List<PersonDetails> personDetails;
    }

    @Data
    public static class PersonDetails {
        private String personName;
        private String position;
        private String comment;
        private String rating;
    }
}


