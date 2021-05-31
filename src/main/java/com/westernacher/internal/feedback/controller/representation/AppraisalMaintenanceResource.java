package com.westernacher.internal.feedback.controller.representation;

import com.westernacher.internal.feedback.domain.AppraisalReviewGoal;
import com.westernacher.internal.feedback.domain.v2.AppraisalRole;
import lombok.Data;

import java.util.List;

public class AppraisalMaintenanceResource {
    @Data
    public static class UnlockRequest {
        String employeeEmail;
        String reviewerEmail;
        String reviewerType;
    }

    @Data
    public static class UnlockResponse {
        List<AppraisalRole> roles;
        List<AppraisalReviewGoal> reviewGoals;
    }
}
