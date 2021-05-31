package com.westernacher.internal.feedback.service;

import com.westernacher.internal.feedback.domain.v1Goal;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface v1GoalService {

    List<v1Goal> uploadGoalCsvFile(MultipartFile file);

}
