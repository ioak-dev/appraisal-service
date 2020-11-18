package com.westernacher.internal.feedback.domain;

import com.bol.secure.Encrypted;
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

    @Encrypted
    private String firstName;

    @Encrypted
    private String lastName;

    @Encrypted
    private Date joiningDate;
    private String cycleId;
    private String cu;

    @Encrypted
    private String job;
    private String unit;

    @Encrypted
    private String status;
    private String email;

    @Encrypted
    private Date lastAppraisalDate;

    @Encrypted
    private Integer duration;

}
