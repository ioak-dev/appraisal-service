package com.westernacher.internal.feedback.service;

import com.westernacher.internal.feedback.domain.*;
import com.westernacher.internal.feedback.service.Implementation.AppraisalCycleResource;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface AppraisalCycleService {

    AppraisalCycle create(AppraisalCycle appraisalCycle);

    AppraisalCycleResource.CycleDeleteResource delete(String id);

    void copyPreviousAppraisalGoals(String sourceCycleId, String destinationCycleId);

    List<String> movetonextlevel(String cycleId, String currentLevel, String employeeId, boolean moveBackwards);

    byte[] printPdf(HttpServletResponse response, List<String> appraisalReviewIds);
}
