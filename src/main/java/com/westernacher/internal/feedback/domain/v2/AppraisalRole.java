package com.westernacher.internal.feedback.domain.v2;

import com.bol.secure.Encrypted;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "appraisal.role")
public class AppraisalRole {
    @Id
    private String id;
    private String reviewerId;
    private String reviewerType;
    private String employeeId;
    private String cycleId;
    private double primaryScore;
    private double secondaryScore;
    private boolean isComplete;

    @CreatedDate
    private Date createdDate;

    @LastModifiedDate
    private Date lastModifiedDate;
}
