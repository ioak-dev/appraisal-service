package com.westernacher.internal.feedback.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Document(collection = "person")
public class Person {

    @Id
    private String id;
    private String empId;
    private String firstName;
    private String lastName;
    private Date joiningDate;
    private String cu;
    private String job;
    private String unit;
    private PersonStatus status;
    private String email;
    private Date lastAppraisalDate;
    private int duration;




}
