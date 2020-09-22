package com.westernacher.internal.feedback.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "appraisal.review")
public class AppraisalReview {
    @Id
    private String id;
    private String cycleId;
    private String employeeId;
    private List<SubjectiveResponse> sectiontwoResponse;
    private List<SubjectiveResponse> sectionthreeResponse;
    private String sectionfourResponse;
    private String sectionfiveResponse;
    private AppraisalStatusType status;
}
