package com.westernacher.internal.feedback.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "appraisal.person")
public class AppraisalPerson {

    @Id
    private String id;
    private String empId;
    private String firstName;
    private String lastName;
    private Date joiningDate;
    private String cycleId;
    private String cu;
    private String job;
    private String unit;
    private PersonStatus status;
    private String email;
    private Date lastAppraisalDate;
    private int duration;




}
