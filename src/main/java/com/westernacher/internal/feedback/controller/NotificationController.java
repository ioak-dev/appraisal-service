package com.westernacher.internal.feedback.controller;


import com.westernacher.internal.feedback.domain.*;
import com.westernacher.internal.feedback.domain.v2.AppraisalRole;
import com.westernacher.internal.feedback.domain.v2.Person;
import com.westernacher.internal.feedback.repository.AppraisalReviewRepository;
import com.westernacher.internal.feedback.repository.AppraisalRoleRepository;
import com.westernacher.internal.feedback.repository.v2.PersonRepository;
import com.westernacher.internal.feedback.service.AppraisalReviewGoalService;
import com.westernacher.internal.feedback.util.MailUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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

    @PostMapping("/send")
    public void sendContactSupport(@RequestBody ContactResource resource) {
        Map<String, String> body= new HashMap<>();
        body.put("body", resource.getBody());

        Map<String, String> subject= new HashMap<>();
        subject.put("subject", resource.getSubject());

        mailUtil.send(resource.getTo(), "contact-body.vm", body,
                "contact-subject.vm", subject);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ContactResource {
        private String body;
        private String signature;
        private String subject;
        private String to;
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

        for (AppraisalReview appraisalReview  : appraisalReviews) {
            List<AppraisalRole> appraisalRoleList = appraisalRoleRepository.findByEmployeeIdAndCycleIdAndReviewerType(appraisalReview.getEmployeeId(),
                    appraisalReview.getCycleId(),AppraisalStatusType.valueOf(appraisalReview.getStatus()));
            appraisalRoleList.stream().forEach(appraisalRole -> {
                if (!appraisalRole.isComplete()) {
                    appraisalRoles.add(appraisalRole);
                }
            });
        }
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
                cycleId, AppraisalStatusType.valueOf(appraisalReview.getStatus())));

        appraisalReviewGoalService.sendMailAfterSubmit(appraisalRoles, personStore);
    }
}


