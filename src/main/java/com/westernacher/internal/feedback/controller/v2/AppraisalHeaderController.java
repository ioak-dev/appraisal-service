
package com.westernacher.internal.feedback.controller.v2;

import com.westernacher.internal.feedback.domain.v2.AppraisalHeader;
import com.westernacher.internal.feedback.repository.v2.AppraisalHeaderRepository;
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

    @GetMapping
    public List<AppraisalHeader> getAll (@RequestParam(required = false) String from,
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
            return response;
        }
        return repository.findAll();
    }

    @GetMapping("/{employeeId}")
    public AppraisalHeader getHeaderByEmployeeId (@PathVariable String employeeId) {
        return repository.findByEmployeeId(employeeId);
    }


    @PostMapping
    public ResponseEntity<AppraisalHeader> create (@RequestBody AppraisalHeader appraisalHeader) {
        AppraisalHeader existingRecord = repository.findByFromAndToAndEmployeeIdAndReviewerId(appraisalHeader.getFrom(),
                appraisalHeader.getTo(),
                appraisalHeader.getEmployeeId(),
                appraisalHeader.getReviewerId());
        if (existingRecord != null) {
            return ResponseEntity.ok(existingRecord);
        }else {
            return ResponseEntity.ok(repository.save(appraisalHeader));
        }
    }
}
