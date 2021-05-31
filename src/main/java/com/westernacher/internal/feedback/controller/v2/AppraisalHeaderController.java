
package com.westernacher.internal.feedback.controller.v2;


import com.westernacher.internal.feedback.domain.v2.AppraisalDescriptive;
import com.westernacher.internal.feedback.repository.v2.AppraisalDescriptiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class AppraisalHeaderController {

    @Autowired
    private AppraisalDescriptiveRepository repository;

    @GetMapping
    public List<AppraisalDescriptive> getAll () {
        return repository.findAll();
    }


    @PostMapping
    public ResponseEntity<AppraisalDescriptive> create (@Valid @RequestBody AppraisalDescriptive appraisalDescriptive) {
        return ResponseEntity.ok(repository.save(appraisalDescriptive));
    }


}
