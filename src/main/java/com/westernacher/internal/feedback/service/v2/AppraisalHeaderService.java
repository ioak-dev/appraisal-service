package com.westernacher.internal.feedback.service.v2;

import com.westernacher.internal.feedback.domain.v2.AppraisalHeader;

import java.util.List;

public interface AppraisalHeaderService {
    List<AppraisalHeader> getHeaderByEmployeeId(String employeeId, String from, String to);

}
