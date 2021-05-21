package com.westernacher.internal.feedback.service;

import com.westernacher.internal.feedback.controller.representation.AppraisalMaintenanceResource;

public interface AppraisalMaintenanceService {

    AppraisalMaintenanceResource.UnlockResponse unlock(String cycleId, String employeeId, String reviewerId, String reviewerType, Boolean simulate);
}
