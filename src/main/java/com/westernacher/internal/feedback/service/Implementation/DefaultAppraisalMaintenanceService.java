package com.westernacher.internal.feedback.service.Implementation;

import com.westernacher.internal.feedback.controller.representation.AppraisalMaintenanceResource;
import com.westernacher.internal.feedback.domain.AppraisalReview;
import com.westernacher.internal.feedback.domain.AppraisalReviewGoal;
import com.westernacher.internal.feedback.domain.v2.AppraisalRole;
import com.westernacher.internal.feedback.repository.*;
import com.westernacher.internal.feedback.repository.v2.PersonRepository;
import com.westernacher.internal.feedback.service.AppraisalMaintenanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class DefaultAppraisalMaintenanceService implements AppraisalMaintenanceService {

    @Autowired
    private AppraisalCycleRepository repository;

    @Autowired
    private v1GoalRepository v1GoalRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private AppraisalPersonRepository appraisalPersonRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AppraisalReviewGoalRepository reviewGoalRepository;

    @Autowired
    private AppraisalReviewRepository appraisalReviewRepository;

    @Autowired
    private AppraisalReviewGoalRepository appraisalReviewGoalRepository;

    @Autowired
    private AppraisalGoalRepository appraisalGoalRepository;

    @Autowired
    private v1AppraisalRoleRepository v1AppraisalRoleRepository;

    @Autowired
    private AppraisalReviewMasterRepository appraisalReviewMasterRepository;

    @Override
    public AppraisalMaintenanceResource.UnlockResponse unlock(String cycleId, String employeeId, String reviewerId, String reviewerType, Boolean simulate) {
        AppraisalMaintenanceResource.UnlockResponse response = new AppraisalMaintenanceResource.UnlockResponse();
        response.setReviewGoals(new ArrayList<>());
        response.setRoles(new ArrayList<>());
        AppraisalReview review = appraisalReviewRepository.findFirstByCycleIdAndEmployeeId(cycleId, employeeId);
        List<AppraisalRole> roleList = v1AppraisalRoleRepository.findAllByCycleIdAndEmployeeIdAndReviewerIdAndReviewerType(cycleId, employeeId, reviewerId, reviewerType);
        roleList.forEach(role -> {
            response.getReviewGoals().addAll(unlockReviewGoals(review.getId(), role.getReviewerId(), role.getReviewerType(), simulate));
            role.setComplete(false);
            response.getRoles().add(role);
            if (!simulate) {
                v1AppraisalRoleRepository.save(role);
            }
        });

        return response;
    }

    private List<AppraisalReviewGoal> unlockReviewGoals(String appraisalId, String reviewerId, String reviewerType, Boolean simulate) {
        List<AppraisalReviewGoal> response = new ArrayList<>();
        List<AppraisalReviewGoal> reviewGoalList = appraisalReviewGoalRepository.findAllByAppraisalIdAndReviewerIdAndReviewerType(appraisalId, reviewerId, reviewerType);
        reviewGoalList.forEach(item -> {
            item.setComplete(false);
            response.add(item);
            if (!simulate) {
                appraisalReviewGoalRepository.save(item);
            }
        });
        return response;
    }
}
