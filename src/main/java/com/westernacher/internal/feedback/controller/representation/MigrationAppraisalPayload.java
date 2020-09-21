package com.westernacher.internal.feedback.controller.representation;

import com.westernacher.internal.feedback.domain.AppraisalGoal;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MigrationAppraisalPayload {
    private List<MigrationAppraisalPerson> persons;
    private List<MigrationAppraisal> appraisals;
    private Map<String, Integer> goalOrder;
}
