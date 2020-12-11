package com.westernacher.internal.feedback.service;

import com.westernacher.internal.feedback.domain.*;
import com.westernacher.internal.feedback.service.Implementation.AppraisalCycleResource;

import java.util.List;

public interface AppraisalCycleService {

    AppraisalCycle create(AppraisalCycle appraisalCycle);

    AppraisalCycleResource.CycleDeleteResource delete(String id);

    void copyPreviousAppraisalGoals(String sourceCycleId, String destinationCycleId);

    List<String> movetonextlevel(String cycleId, String currentLevel, String employeeId, boolean moveBackwards);
}
