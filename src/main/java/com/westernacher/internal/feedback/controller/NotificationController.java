package com.westernacher.internal.feedback.controller;


import com.westernacher.internal.feedback.domain.*;
import com.westernacher.internal.feedback.repository.AppraisalReviewRepository;
import com.westernacher.internal.feedback.repository.AppraisalRoleRepository;
import com.westernacher.internal.feedback.repository.PersonRepository;
import com.westernacher.internal.feedback.service.AppraisalReviewGoalService;
import com.westernacher.internal.feedback.util.MailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/notification")
@Slf4j
public class NotificationController {

    @Autowired
    private AppraisalReviewRepository appraisalReviewRepository;

    @Autowired
    private AppraisalRoleRepository appraisalRoleRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private AppraisalReviewGoalService appraisalReviewGoalService;

    @Autowired
    private MailUtil mailUtil;

    @PostMapping("/send/{to}")
    public void send(@PathVariable String to) {
        mailUtil.send(to, "test-body.vm", new HashMap<>(),
                "test-subject.vm", new HashMap<>());
    }

    @PostMapping("/send/all/{cycleId}")
    public void sendMailToListOfAppraisal(@RequestBody List<String> employeeIds, @PathVariable String cycleId) {
        Map<String, Person> personStore = new HashMap<>();
        List<Person> personList = personRepository.findAll();
        personList.stream().forEach(person -> {
            personStore.put(person.getId(), person);
        });
        List<AppraisalReview> appraisalReviews = appraisalReviewRepository.findAllByCycleIdAndEmployeeIdIn(cycleId, employeeIds);
        List<AppraisalRole> appraisalRoles = new ArrayList<>();

        appraisalReviews.stream().forEach(appraisalReview -> {
            appraisalRoles.add(appraisalRoleRepository.findByEmployeeIdAndCycleIdAndReviewerTypeAndCompleteIs(appraisalReview.getEmployeeId(),
                    appraisalReview.getCycleId(),appraisalReview.getStatus(), false));
        });

        appraisalReviewGoalService.sendMailAfterSubmit(appraisalRoles, personStore);

    }

    @PostMapping("/send/one/{cycleId}/{appraisalId}")
    public void sendMailToOnefAppraisal(@PathVariable String cycleId, @PathVariable String appraisalId) {
        Map<String, Person> personStore = new HashMap<>();
        List<Person> personList = personRepository.findAll();
        personList.stream().forEach(person -> {
            personStore.put(person.getId(), person);
        });
        AppraisalReview appraisalReview = appraisalReviewRepository.findById(appraisalId).orElse(null);
        List<AppraisalRole> appraisalRoles = new ArrayList<>();

        appraisalRoles.addAll(appraisalRoleRepository.findByEmployeeIdAndCycleIdAndReviewerType(appraisalReview.getEmployeeId(),
                cycleId, appraisalReview.getStatus()));

        appraisalReviewGoalService.sendMailAfterSubmit(appraisalRoles, personStore);
    }
}


