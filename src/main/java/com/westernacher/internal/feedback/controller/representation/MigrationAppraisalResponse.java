package com.westernacher.internal.feedback.controller.representation;

import com.westernacher.internal.feedback.domain.AppraisalGoal;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MigrationAppraisalResponse {
    private List<AppraisalGoal> appraisalGoals = new ArrayList<>();
    public void addAppraisalGoal(AppraisalGoal appraisalGoal) {
        appraisalGoals.add(appraisalGoal);
    }
}
