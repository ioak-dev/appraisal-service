package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.controller.representation.ReportResource;
import com.westernacher.internal.feedback.domain.AppraisalCycle;
import com.westernacher.internal.feedback.domain.v2.AppraisalRole;
import com.westernacher.internal.feedback.domain.AppraisalStatusType;
import com.westernacher.internal.feedback.domain.v2.Person;
import com.westernacher.internal.feedback.repository.AppraisalCycleRepository;
import com.westernacher.internal.feedback.repository.AppraisalRoleRepository;
import com.westernacher.internal.feedback.repository.v2.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private AppraisalRoleRepository approsalRoleRepository;
    @Autowired
    private AppraisalCycleRepository appraisalCycleRepository;

    @Autowired
    private PersonRepository personRepository;

    @GetMapping("/summary/{cycleId}")
    public List<ReportResource.Summary> getSummary (@PathVariable String cycleId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName().toLowerCase();
        Person person = personRepository.findPersonByEmail(name);
        AppraisalCycle cycle = appraisalCycleRepository.findById(cycleId).get();

        List<Person> personList = personRepository.findAll();
        Map<String, Person> personMap = new HashMap();
        personList.stream().forEach(item -> {
            personMap.put(item.getId(),item);
        });

        List<AppraisalRole> appraisalRoleList= approsalRoleRepository.findAllByCycleId(cycleId);
        Set<String> employeeIds = new HashSet();
        appraisalRoleList.stream().forEach(item ->{
           if (item.getEmployeeId().equals(person.getId()) || item.getReviewerId().equals(person.getId())) {
               employeeIds.add(item.getEmployeeId());
           }
        });

        List<ReportResource.Summary> summaryList = new ArrayList<>();

        appraisalRoleList.stream().forEach(item ->{
            if(employeeIds.contains(item.getEmployeeId())) {
                ReportResource.Summary summary = new ReportResource.Summary();
                summary.setCycleId(cycleId);
                summary.setCycleName(cycle.getName());
                summary.setEmployeeId(personMap.get(item.getEmployeeId()).getEmpId());
                summary.setEmployeeFirstName(personMap.get(item.getEmployeeId()).getFirstName());
                summary.setEmployeeLastName(personMap.get(item.getEmployeeId()).getLastName());
                summary.setEmployeeEmail(personMap.get(item.getEmployeeId()).getEmail());
                summary.setJob(personMap.get(item.getEmployeeId()).getJob());
                summary.setCu(personMap.get(item.getEmployeeId()).getCu());
                summary.setReviewerFirstName(personMap.get(item.getReviewerId()).getFirstName());
                summary.setReviewerLastName(personMap.get(item.getReviewerId()).getLastName());
                summary.setReviewerEmail(personMap.get(item.getReviewerId()).getEmail());
                summary.setReviewerType(cycle.getWorkflowMap().get(AppraisalStatusType.valueOf(item.getReviewerType())));
                summary.setPrimaryScore(round(item.getPrimaryScore(), 2));
                summary.setSecondaryScore(round(item.getSecondaryScore(), 2));
                summary.setCompletionStatus(item.isComplete() ? "Complete" : "Not complete");
                summaryList.add(summary);
            }
        });
        return summaryList;
    }

    private static double round(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }
}


