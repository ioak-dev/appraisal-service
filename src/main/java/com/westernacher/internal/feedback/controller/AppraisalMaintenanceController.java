package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.controller.representation.AppraisalMaintenanceResource;
import com.westernacher.internal.feedback.domain.Appraisal;
import com.westernacher.internal.feedback.domain.AppraisalReviewMaster;
import com.westernacher.internal.feedback.domain.AppraisalRole;
import com.westernacher.internal.feedback.domain.Person;
import com.westernacher.internal.feedback.repository.AppraisalRepository;
import com.westernacher.internal.feedback.repository.AppraisalReviewMasterRepository;
import com.westernacher.internal.feedback.repository.AppraisalRoleRepository;
import com.westernacher.internal.feedback.repository.PersonRepository;
import com.westernacher.internal.feedback.service.AppraisalMaintenanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/maintain")
@Slf4j
public class AppraisalMaintenanceController {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private AppraisalRoleRepository appraisalRoleRepository;

    @Autowired
    private AppraisalRepository appraisalRepository;

    @Autowired
    private AppraisalReviewMasterRepository appraisalReviewMasterRepository;

    @Autowired
    private AppraisalMaintenanceService service;

    @PostMapping("/unlock/{cycleId}")
    public List<AppraisalMaintenanceResource.UnlockResponse> unlock(
            @PathVariable String cycleId,
            @RequestParam(required = false) Boolean simulate,
            @RequestBody List<AppraisalMaintenanceResource.UnlockRequest> unlockRequestList) {
        List<Person> personList = personRepository.findAll();
        Map<String, String> personMap = new HashMap<>();
        personList.forEach(item -> personMap.put(item.getEmail(), item.getId()));

        List<AppraisalMaintenanceResource.UnlockResponse> response = new ArrayList<>();

        Boolean doSimulate = true;

        if (simulate != null && simulate == false) {
            doSimulate = false;
        }

        for (AppraisalMaintenanceResource.UnlockRequest unlockRequest : unlockRequestList) {
            AppraisalMaintenanceResource.UnlockResponse unlockResponse = service.unlock(
                    cycleId,
                    personMap.get(unlockRequest.getEmployeeEmail()),
                    personMap.get(unlockRequest.getReviewerEmail()),
                    unlockRequest.getReviewerType(),
                    doSimulate
            );
            response.add(unlockResponse);
        }

        return response;
    }

    @GetMapping("/fixmasterstatus")
    public List<AppraisalRole> fixMasterStatus(@RequestParam(required = false) Boolean simulate) {
        List<AppraisalReviewMaster> masterList = appraisalReviewMasterRepository.findAll();
        List<Appraisal> appraisalList = appraisalRepository.findAll();
        List<AppraisalRole> roles = appraisalRoleRepository.findAll();
        Map<String, AppraisalRole> rolesMap = new HashMap<>();
        Map<String, String> appraisalMap = new HashMap<>();
        for (AppraisalRole role : roles) {
            if (role.getReviewerType() != null && role.getReviewerType().equals("Master")) {
                rolesMap.put(role.getCycleId() + '-' + role.getEmployeeId() + '-' + role.getReviewerId(), role);
            }
        }
        for (Appraisal appraisal : appraisalList) {
            appraisalMap.put(appraisal.getId(), appraisal.getCycleId());
        }
        List<AppraisalRole> rolesToUpdate = new ArrayList<>();
        for(AppraisalReviewMaster master : masterList) {
            if (master.isComplete()) {
                String key = appraisalMap.get(master.getAppraisalId()) + "-" + master.getEmployeeId() + '-' + master.getReviewerId();
//                if (rolesMap.containsKey(key) && !rolesMap.get(key).isComplete()) {
                if (rolesMap.containsKey(key)) {
                    rolesMap.get(key).setPrimaryScore(Double.parseDouble(master.getRating().substring(0,1)));
                    rolesToUpdate.add(rolesMap.get(key));
                }
            }
        }

        log.info(rolesToUpdate.size() + "--" + masterList.size() + "--" + roles.size());

        if (!simulate) {
            for (AppraisalRole role : rolesToUpdate) {
                role.setComplete(true);
                appraisalRoleRepository.save(role);
            }
        }

        return rolesToUpdate;
    }
}


