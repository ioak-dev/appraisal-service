package com.westernacher.internal.feedback.service.v2;


import com.westernacher.internal.feedback.domain.v1Goal;

import java.util.List;

public interface GoalService {

    List<v1Goal> getGoalsForEmployee(String employeeId);

}
