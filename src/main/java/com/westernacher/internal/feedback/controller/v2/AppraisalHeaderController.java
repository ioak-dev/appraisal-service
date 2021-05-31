
package com.westernacher.internal.feedback.controller.v2;

import com.westernacher.internal.feedback.domain.v2.AppraisalHeader;
import com.westernacher.internal.feedback.repository.v2.AppraisalHeaderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/header")
@Slf4j
public class AppraisalHeaderController {

    @Autowired
    private AppraisalHeaderRepository repository;

    @GetMapping
    public List<AppraisalHeader> getAll () {
        return repository.findAll();
    }


    @PostMapping
    public ResponseEntity<String> create (@RequestBody AppraisalHeader appraisalHeader) {
        AppraisalHeader existingRecord = repository.findByFromAndToAndEmployeeIdAndReviewerId(appraisalHeader.getFrom(),
                appraisalHeader.getTo(),
                appraisalHeader.getEmployeeId(),
                appraisalHeader.getReviewerId());
        if (existingRecord != null) {
            return ResponseEntity.ok(existingRecord.getId());
        }else {
            return ResponseEntity.ok(repository.save(appraisalHeader).getId());
        }

    }


}
