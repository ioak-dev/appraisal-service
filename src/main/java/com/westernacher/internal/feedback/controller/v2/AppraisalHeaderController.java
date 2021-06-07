
package com.westernacher.internal.feedback.controller.v2;

import com.westernacher.internal.feedback.domain.v2.AppraisalHeader;
import com.westernacher.internal.feedback.repository.v2.AppraisalHeaderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
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
            try {
                Date dateFrom=new SimpleDateFormat("dd/MM/yyyy").parse(from);
                Date dateTo=new SimpleDateFormat("dd/MM/yyyy").parse(to);
                List<AppraisalHeader> appraisalHeaders = repository.findAll();
                List<AppraisalHeader> response = new ArrayList<>();
                for (AppraisalHeader appraisalHeader:appraisalHeaders) {
                    Date appraisalFrom = new SimpleDateFormat("dd/MM/yyyy").parse(appraisalHeader.getFrom().toString());
                    Date appraisalTo = new SimpleDateFormat("dd/MM/yyyy").parse(appraisalHeader.getTo().toString());
                    if ((appraisalFrom.before(dateFrom) && appraisalTo.after(dateFrom)) ||
                            (appraisalFrom.after(dateFrom) && appraisalTo.before(dateTo)) ||
                            (appraisalFrom.before(dateTo) && appraisalTo.after(dateTo))) {
                        response.add(appraisalHeader);
                    }

                }
                return response;
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
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
