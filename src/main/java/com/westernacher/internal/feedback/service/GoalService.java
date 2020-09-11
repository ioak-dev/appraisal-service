package com.westernacher.internal.feedback.service;

import com.westernacher.internal.feedback.domain.Goal;
import com.westernacher.internal.feedback.domain.Role;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GoalService {

    List<Goal> uploadGoalCsvFile(MultipartFile file);

}
