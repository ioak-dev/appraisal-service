package com.westernacher.internal.feedback.service;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
class CsvObject {
    private String userId;
    private String status;
    private String cycleId;
    private String sectiononeResponseGroup;
    private String sectiononeResponseResponseCriteria;
    private String sectiononeResponseResponseDescription;
    private String sectiononeResponseResponseSelfComment;
    private String sectiononeResponseResponseSelfRating;
    private Double sectiononeResponseResponseWeightage;

    private String sectiononeResponseHrReviewsComment;
    private Boolean sectiononeResponseHrReviewsComplete;
    private String sectiononeResponseHrReviewsName;
    private String sectiononeResponseHrReviewsRating;

    /*private String sectiononeResponsePracticeDirectorReviewsComment;
    private Boolean sectiononeResponsePracticeDirectorReviewsComplete;
    private String sectiononeResponsePracticeDirectorReviewsName;
    private String sectiononeResponsePracticeDirectorReviewsRating;

    private String sectiononeResponseProjectManagerReviewsComment;
    private Boolean sectiononeResponseProjectManagerReviewsComplete;
    private String sectiononeResponseProjectManagerReviewsName;
    private String sectiononeResponseProjectManagerReviewsRating;

    private String sectiononeResponseTeamLeadReviewsComment;
    private Boolean sectiononeResponseTeamLeadReviewsComplete;
    private String sectiononeResponseTeamLeadReviewsName;
    private String sectiononeResponseTeamLeadReviewsRating;*/



}


