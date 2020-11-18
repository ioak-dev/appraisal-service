package com.westernacher.internal.feedback.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MigrationAppraisalPerson {

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
    private String status;

}
