package com.westernacher.internal.feedback.service;

import com.westernacher.internal.feedback.domain.*;
import com.westernacher.internal.feedback.service.Implementation.AppraisalCycleResource;

public interface AppraisalCycleService {

    AppraisalCycle create(AppraisalCycle appraisalCycle);

    AppraisalCycleResource.CycleDeleteResource delete(String appraisalCycle);

}
