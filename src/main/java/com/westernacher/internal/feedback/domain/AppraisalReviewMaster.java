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
@Document(collection = "appraisal.review.master")
public class AppraisalReviewMaster {
    @Id
    private String id;
    private String employeeId;
    private String appraisalId;
    private String reviewerId;

    @Encrypted
    private String comment;

    @Encrypted
    private String rating;
    @Encrypted
    private boolean isComplete;
}
