package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.controller.representation.ReportResource;
import com.westernacher.internal.feedback.domain.AppraisalCycle;
import com.westernacher.internal.feedback.domain.AppraisalRole;
import com.westernacher.internal.feedback.domain.AppraisalStatusType;
import com.westernacher.internal.feedback.domain.Person;
import com.westernacher.internal.feedback.repository.AppraisalCycleRepository;
import com.westernacher.internal.feedback.repository.AppraisalRoleRepository;
import com.westernacher.internal.feedback.repository.PersonRepository;
import com.westernacher.internal.feedback.service.AppraisalCycleService;
import com.westernacher.internal.feedback.service.Implementation.AppraisalCycleResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private AppraisalRoleRepository approsalRoleRepository;

    @Autowired
    private PersonRepository personRepository;

    @GetMapping("/summary/{cycleId}")
    public List<ReportResource.Summary> getSummary (@PathVariable String cycleId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Person person = personRepository.findPersonByEmail(name);

        List<Person> personList = personRepository.findAll();
        Map<String, Person> personMap = new HashMap();
        personList.stream().forEach(item -> {
            personMap.put(item.getId(),item);
        });

        List<AppraisalRole> appraisalRoleList= approsalRoleRepository.findAllByCycleId(cycleId);
        Set<String> employeeIds = new HashSet();
        appraisalRoleList.stream().forEach(item ->{
           if (item.getEmployeeId().equals(person.getId())) {
               employeeIds.add(item.getEmployeeId());
           }

           if (item.getReviewerId().equals(person.getId())) {
               employeeIds.add(item.getReviewerId());
           }
        });

        List<ReportResource.Summary> summaryList = new ArrayList<>();

        appraisalRoleList.stream().forEach(item ->{
            if(employeeIds.contains(item.getEmployeeId())) {
                ReportResource.Summary summary = new ReportResource.Summary();
                summary.setCycleId(cycleId);
                summary.setEmployeeId(item.getEmployeeId());
                summary.setEmployeeName(personMap.get(item.getEmployeeId()).getFirstName());
                summary.setEmployeeEmail(personMap.get(item.getEmployeeId()).getEmail());
                summary.setJob(personMap.get(item.getEmployeeId()).getJob());
                summary.setCu(personMap.get(item.getEmployeeId()).getCu());
                summary.setReviewerName(personMap.get(item.getReviewerId()).getFirstName());
                summary.setReviewerEmail(personMap.get(item.getReviewerId()).getEmail());
                summary.setReviewerType(AppraisalStatusType.valueOf(item.getReviewerType()));
                summary.setPrimaryScore(item.getPrimaryScore());
                summary.setSecondaryScore(item.getSecondaryScore());
                summary.setIsComplete(item.isComplete());
                summaryList.add(summary);
            }
        });
        return summaryList;
    }
}


