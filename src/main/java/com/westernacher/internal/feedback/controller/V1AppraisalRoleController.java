
package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.domain.v2.AppraisalRole;
import com.westernacher.internal.feedback.domain.AppraisalStatusType;
import com.westernacher.internal.feedback.repository.AppraisalRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/appraisal/role")
@Slf4j
public class V1AppraisalRoleController {

    @Autowired
    private AppraisalRoleRepository repository;

    @GetMapping
    public ResponseEntity<?> getAppraisalRoles (@RequestParam String cycleId) {

        List<AppraisalRole> appraisalRoleList= repository.findAllByCycleId(cycleId);

        Comparator<AppraisalRole> appraisalRoleComparator = Comparator
                .comparing(AppraisalRole::getEmployeeId)
                .thenComparing((AppraisalRole ARG) -> AppraisalStatusType.valueOf(ARG.getReviewerType()).ordinal())
                .thenComparing(AppraisalRole::isComplete);
        List<AppraisalRole> sortedEmployees = appraisalRoleList.stream()
                .sorted(appraisalRoleComparator)
                .collect(Collectors.toList());
        return ResponseEntity.ok(sortedEmployees);
    }
}
