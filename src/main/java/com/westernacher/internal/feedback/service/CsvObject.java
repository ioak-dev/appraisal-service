package com.westernacher.internal.feedback.service;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CsvObject {
    private String userId;
    private String status;
    private String name;
    private String email;
    private String criteria;
    private String group;
    private String weightage;
    private String selfComment;
    private String selfRating;
    private double score;

    private String projectManagerComment1;
    private String projectManagerComplete1;
    private String projectManagerName1;
    private String projectManagerRating1;

    private String projectManagerComment2;
    private String projectManagerComplete2;
    private String projectManagerName2;
    private String projectManagerRating2;

    private String projectManagerComment3;
    private String projectManagerComplete3;
    private String projectManagerName3;
    private String projectManagerRating3;

    private String projectManagerComment4;
    private String projectManagerComplete4;
    private String projectManagerName4;
    private String projectManagerRating4;

    private String projectManagerComment5;
    private String projectManagerComplete5;
    private String projectManagerName5;
    private String projectManagerRating5;

    private String teamLeadComment1;
    private String teamLeadComplete1;
    private String teamLeadName1;
    private String teamLeadRating1;

    private String teamLeadComment2;
    private String teamLeadComplete2;
    private String teamLeadName2;
    private String teamLeadRating2;

    private String practiceDirectorComment1;
    private String practiceDirectorComplete1;
    private String practiceDirectorName1;
    private String practiceDirectorRating1;

    private String practiceDirectorComment2;
    private String practiceDirectorComplete2;
    private String practiceDirectorName2;
    private String practiceDirectorRating2;

    private String hrComment;
    private String hrComplete;
    private String hrName;
    private String hrRating;
}


