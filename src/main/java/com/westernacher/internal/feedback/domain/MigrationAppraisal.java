package com.westernacher.internal.feedback.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MigrationAppraisal {
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
