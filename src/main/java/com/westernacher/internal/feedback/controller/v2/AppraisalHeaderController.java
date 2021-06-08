
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

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<AppraisalHeader>> getHeaderByEmployeeId (@PathVariable String employeeId) {
        return ResponseEntity.ok(repository.findAllByEmployeeId(employeeId));
    }


    @PostMapping
    public ResponseEntity<AppraisalHeader> create (@RequestBody AppraisalHeader appraisalHeader) {
        return ResponseEntity.ok(repository.save(appraisalHeader));
    }
}
