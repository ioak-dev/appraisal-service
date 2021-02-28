package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.controller.representation.AppraisalMaintenanceResource;
import com.westernacher.internal.feedback.domain.Person;
import com.westernacher.internal.feedback.repository.PersonRepository;
import com.westernacher.internal.feedback.service.AppraisalMaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/maintain")
public class AppraisalMaintenanceController {

    @Autowired
    private PersonRepository personRepository;

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
    }}


