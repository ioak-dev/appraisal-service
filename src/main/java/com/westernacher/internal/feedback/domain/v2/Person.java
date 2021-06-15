package com.westernacher.internal.feedback.domain.v2;

import com.bol.secure.Encrypted;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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
    private String job;
    private String status;
    private Date lastAppraisalDate;
    private Integer duration;
    private String email;
    private String cu;
    private String unit;

    @CreatedDate
    private Date createdDate;

    @LastModifiedDate
    private Date lastModifiedDate;

}
