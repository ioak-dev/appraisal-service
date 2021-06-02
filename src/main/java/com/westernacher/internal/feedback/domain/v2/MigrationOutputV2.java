package com.westernacher.internal.feedback.domain.v2;

import com.westernacher.internal.feedback.domain.AppraisalGoal;
import com.westernacher.internal.feedback.domain.AppraisalReview;
import com.westernacher.internal.feedback.domain.AppraisalReviewGoal;
import com.westernacher.internal.feedback.domain.AppraisalReviewMaster;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MigrationOutputV2 {
    Map<String,List<AppraisalHeader>> appraisalHeaderMap;
    Map<String,List<AppraisalLong>> appraisalLongMap;
    Map<String,List<GoalEmployee>> goalEmployeeMap;
}
