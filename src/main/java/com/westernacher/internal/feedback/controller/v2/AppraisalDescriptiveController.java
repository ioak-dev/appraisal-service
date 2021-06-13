
package com.westernacher.internal.feedback.controller.v2;


import com.westernacher.internal.feedback.domain.v2.AppraisalDescriptive;
import com.westernacher.internal.feedback.domain.v2.AppraisalLong;
import com.westernacher.internal.feedback.repository.v2.AppraisalDescriptiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/descriptive")
@Slf4j
public class AppraisalDescriptiveController {

    @Autowired
    private AppraisalDescriptiveRepository repository;

    @GetMapping
    public ResponseEntity<List<AppraisalDescriptive>> getAll (@RequestParam(required = false) String headerId) {
        if (headerId != null) {
            return ResponseEntity.ok(repository.findAllByHeaderId(headerId));
        }
        return ResponseEntity.ok(repository.findAll());
    }


    @PostMapping
    public ResponseEntity<List<AppraisalDescriptive>> create (@RequestBody List<AppraisalDescriptive> appraisalDescriptives,
                                                       @RequestParam String headerId) {

        List<AppraisalDescriptive> appraisalDescriptiveList = new ArrayList<>();
        appraisalDescriptives.stream().forEach(appraisalLong -> {
            appraisalLong.setHeaderId(headerId);
            appraisalDescriptiveList.add(appraisalLong);
        });

        return ResponseEntity.ok(repository.saveAll(appraisalDescriptiveList));
    }


}
