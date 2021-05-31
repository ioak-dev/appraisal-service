
package com.westernacher.internal.feedback.controller.v2;


import com.westernacher.internal.feedback.domain.v2.AppraisalLong;
import com.westernacher.internal.feedback.repository.v2.AppraisalLongRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/appraisal/custom/appraisallong")
@Slf4j
public class AppraisalLongController {

    @Autowired
    private AppraisalLongRepository repository;

    @GetMapping
    public List<AppraisalLong> getAll () {
        return repository.findAll();
    }


    @PostMapping
    public ResponseEntity<AppraisalLong> create (@Valid @RequestBody AppraisalLong appraisalLong) {
        return ResponseEntity.ok(repository.save(appraisalLong));
    }


}
