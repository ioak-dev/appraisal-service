
package com.westernacher.internal.feedback.controller.v1;


import com.westernacher.internal.feedback.domain.v1.AppraisalObjective;
import com.westernacher.internal.feedback.domain.v1.AppraisalSubjective;
import com.westernacher.internal.feedback.repository.v1.AppraisalObjectiveRepositoryNew;
import com.westernacher.internal.feedback.repository.v1.AppraisalSubjectiveRepositoryNew;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class AppraisalSubjectiveControllerNew {

    @Autowired
    private AppraisalSubjectiveRepositoryNew repository;

    @GetMapping
    public List<AppraisalSubjective> getAll () {
        return repository.findAll();
    }


    @PostMapping
    public ResponseEntity<AppraisalSubjective> create (@Valid @RequestBody AppraisalSubjective appraisalSubjective) {
        return ResponseEntity.ok(repository.save(appraisalSubjective));
    }


}
