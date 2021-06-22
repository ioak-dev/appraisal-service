package com.westernacher.internal.feedback.controller.representation;

import com.westernacher.internal.feedback.domain.v2.AppraisalDescriptive;
import com.westernacher.internal.feedback.domain.v2.AppraisalHeader;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
@Builder
public class AppraisalDescriptiveResource {
    private String id;
    private Integer from;
    private Integer to;
    private String employeeId;
    private String reviewerId;
    private String reviewerType;
    private String start;
    private String stop;
    private String toContinue;
    private Date createdDate;
    private Date lastModifiedDate;


    public static AppraisalDescriptiveResource getAppraisalDescriptiveResource(AppraisalDescriptive appraisalDescriptive,
                                                                        Map<String, AppraisalHeader> appraisalHeaderMap) {

        AppraisalHeader appraisalHeader = appraisalHeaderMap.get(appraisalDescriptive.getHeaderId());
        if (appraisalHeader != null && appraisalHeader.getId() != null) {
            return AppraisalDescriptiveResource.builder()
                    .id(appraisalDescriptive.getId())
                    .from(appraisalHeader.getFrom())
                    .to(appraisalHeader.getTo())
                    .employeeId(appraisalHeader.getEmployeeId())
                    .reviewerId(appraisalHeader.getReviewerId())
                    .reviewerType(appraisalHeader.getReviewerType())
                    .start(appraisalDescriptive.getStart())
                    .stop(appraisalDescriptive.getStop())
                    .toContinue(appraisalDescriptive.getToContinue())
                    .createdDate(appraisalDescriptive.getCreatedDate())
                    .lastModifiedDate(appraisalDescriptive.getLastModifiedDate())
                    .build();
        }
       return null;

    }
}
