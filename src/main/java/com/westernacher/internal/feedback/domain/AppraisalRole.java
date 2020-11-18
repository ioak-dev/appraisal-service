package com.westernacher.internal.feedback.domain;

import com.bol.secure.Encrypted;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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

    @Encrypted
    private double primaryScore;

    @Encrypted
    private double secondaryScore;

    @Encrypted
    private boolean isComplete;
}
