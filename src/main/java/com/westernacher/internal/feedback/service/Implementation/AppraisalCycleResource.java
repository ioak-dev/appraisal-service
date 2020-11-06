package com.westernacher.internal.feedback.service.Implementation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class AppraisalCycleResource {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CycleDeleteResource {
        private long deletedRoles;
        private long deletedGoals;
        private long deletedAppraisalReviewGoals;
        private long deletedAppraisalReviewMasters;
        private long deletedAppraisalReviews;
        private long deletedCycle;

    }
}
