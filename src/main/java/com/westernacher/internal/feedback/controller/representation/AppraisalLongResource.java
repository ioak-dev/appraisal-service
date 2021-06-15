package com.westernacher.internal.feedback.controller.representation;

import com.westernacher.internal.feedback.domain.v2.AppraisalHeader;
import com.westernacher.internal.feedback.domain.v2.AppraisalLong;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AppraisalLongResource {
    private String id;
    private Integer from;
    private Integer to;
    private String employeeId;
    private String reviewerId;
    private String reviewerType;
    private Integer orderId;
    private Integer rating;
    private String comment;


    public static AppraisalLongResource getAppraisalLongResource(AppraisalLong appraisalLong,
                                                                 Map<String, AppraisalHeader> appraisalHeaderMap) {

        AppraisalHeader appraisalHeader = appraisalHeaderMap.get(appraisalLong.getHeaderId());
        if (appraisalHeader != null && appraisalHeader.getId() != null) {
            return AppraisalLongResource.builder()
                    .id(appraisalLong.getId())
                    .from(appraisalHeader.getFrom())
                    .to(appraisalHeader.getTo())
                    .employeeId(appraisalHeader.getEmployeeId())
                    .reviewerId(appraisalHeader.getReviewerId())
                    .reviewerType(appraisalHeader.getReviewerType())
                    .orderId(appraisalLong.getOrderId())
                    .rating(appraisalLong.getRating())
                    .comment(appraisalLong.getComment())
                    .build();
        }
       return null;

    }
}
