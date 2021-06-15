package com.westernacher.internal.feedback.domain;

import com.bol.secure.Encrypted;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@Data
@Document(collection = "person")
public class Person {

    @Id
    private String id;

    @Encrypted
    private String empId;

    @Encrypted
    private String firstName;

    @Encrypted
    private String lastName;

    @Encrypted
    private Date joiningDate;

    @Encrypted
    private String job;

    @Encrypted
    private String status;

    @Encrypted
    private Date lastAppraisalDate;

    @Encrypted
    private Integer duration;

    private String email;
    private String cu;
    private String unit;

}
