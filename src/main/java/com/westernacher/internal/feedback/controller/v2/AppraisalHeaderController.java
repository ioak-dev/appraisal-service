
package com.westernacher.internal.feedback.controller.v2;

import com.westernacher.internal.feedback.domain.Role;
import com.westernacher.internal.feedback.domain.v2.AppraisalHeader;
import com.westernacher.internal.feedback.repository.RoleRepository;
import com.westernacher.internal.feedback.repository.v2.AppraisalHeaderRepository;
import com.westernacher.internal.feedback.service.v2.AppraisalHeaderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/header")
@Slf4j
public class AppraisalHeaderController {

    @Autowired
    private AppraisalHeaderRepository repository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AppraisalHeaderService service;

    @GetMapping
    public ResponseEntity<List<AppraisalHeader>> getAll (@RequestParam(required = false) String from,
                                         @RequestParam(required = false) String to) {
        if (from != null && to != null) {
            List<AppraisalHeader> response = new ArrayList<>();
            for (AppraisalHeader appraisalHeader:repository.findAll()) {
                if ((appraisalHeader.getFrom()< Integer.parseInt(from) && appraisalHeader.getTo()> Integer.parseInt(from)) ||
                        (appraisalHeader.getFrom()> Integer.parseInt(from) && appraisalHeader.getTo()<Integer.parseInt(to)) ||
                        (appraisalHeader.getFrom() < Integer.parseInt(to) && appraisalHeader.getTo() >Integer.parseInt(to))) {
                    response.add(appraisalHeader);
                }
            }
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping(value = "/custom")
    public ResponseEntity<List<AppraisalHeader>> getByEmployeeId (@RequestParam String employeeId,
                                                                  @RequestParam String from,
                                                                  @RequestParam String to) {


        return ResponseEntity.ok(service.getHeaderByEmployeeId(employeeId, from, to));

    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<AppraisalHeader>> getHeaderByEmployeeId (@PathVariable String employeeId) {
        return ResponseEntity.ok(repository.findAllByEmployeeId(employeeId));
    }


    @PostMapping
    public ResponseEntity<AppraisalHeader> create (@RequestBody AppraisalHeader appraisalHeader) {
        if(appraisalHeader.getEmployeeId().equals(appraisalHeader.getReviewerId())) {
            appraisalHeader.setReviewerType("Self");
        } else {
            List<Role> roles = roleRepository.findAllByEmployeeIdAndReviewerId(appraisalHeader.getEmployeeId(),
                    appraisalHeader.getReviewerId());
            if (roles != null && roles.size()>1) {
                appraisalHeader.setReviewerType(roles.get(0).getReviewerType());
            } else {
                appraisalHeader.setReviewerType("Level_1");
            }
        }
        return ResponseEntity.ok(repository.save(appraisalHeader));
    }
}
