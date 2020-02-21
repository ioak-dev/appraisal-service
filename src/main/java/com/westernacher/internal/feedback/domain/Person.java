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
    private String name;
    private String jobName;
    private String email;
    private String unit;
    private Date joiningDate;
    private String level;
    private String specialization;
    private Date lastAppraisalDate;
    private int duration;
    private List<Role> roles;
    private PersonStatus status;

}
