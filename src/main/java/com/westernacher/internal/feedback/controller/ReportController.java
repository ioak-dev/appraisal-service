package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.domain.AppraisalCycle;
import com.westernacher.internal.feedback.domain.AppraisalRole;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private AppraisalCycleRepository repository;

    @Autowired
    private AppraisalRoleRepository approsalRoleRepository;

    @Autowired
    private PersonRepository personRepository;

    @GetMapping("/summary/{cycleId}")
    public void get (@PathVariable String cycleId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Person person = personRepository.findPersonByEmail(name);

        List<Person> personList = personRepository.findAll();

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

    }


}


