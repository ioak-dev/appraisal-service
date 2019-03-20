package com.westernacher.internal.feedback.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@Document(collection = "appraisal")
public class Appraisal {
    @Id
    private String id;
    private String cycleId;
    private String userId;
    private List<ObjectiveResponseGroup> sectiononeResponse;
    private List<SubjectiveResponse> sectiontwoResponse;
    private List<SubjectiveResponse> sectionthreeResponse;
    private String sectionfourResponse;
    private String sectionfiveResponse;
    private AppraisalStatusType status;
}
