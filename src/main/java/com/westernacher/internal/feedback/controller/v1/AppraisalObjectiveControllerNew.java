
package com.westernacher.internal.feedback.controller.v1;


import com.westernacher.internal.feedback.domain.AppraisalCycle;
import com.westernacher.internal.feedback.domain.v1.AppraisalObjective;
import com.westernacher.internal.feedback.repository.v1.AppraisalObjectiveRepositoryNew;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class AppraisalObjectiveControllerNew {

    @Autowired
    private AppraisalObjectiveRepositoryNew repository;

    @GetMapping
    public List<AppraisalObjective> getAll () {
        return repository.findAll();
    }


    @PostMapping
    public ResponseEntity<AppraisalObjective> create (@Valid @RequestBody AppraisalObjective appraisalObjective) {
        return ResponseEntity.ok(repository.save(appraisalObjective));
    }


}
