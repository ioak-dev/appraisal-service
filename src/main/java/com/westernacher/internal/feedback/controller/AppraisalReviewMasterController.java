
package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.domain.AppraisalReviewMaster;
import com.westernacher.internal.feedback.repository.AppraisalReviewMasterRepository;
import com.westernacher.internal.feedback.service.AppraisalReviewGoalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appraisal/review/master")
@Slf4j
public class AppraisalReviewMasterController {

    @Autowired
    private AppraisalReviewGoalService service;

    private AppraisalReviewMasterRepository repository;

    @GetMapping
    public ResponseEntity<?> getReviewMaster (@RequestParam String appraisalId) {
        return ResponseEntity.ok(repository.findAllByAppraisalId(appraisalId));
    }

    @PutMapping
    public ResponseEntity<?> update (@RequestBody AppraisalReviewMaster reviewMaster) {
        if (reviewMaster.getId() == null) {
            return ResponseEntity.ok(repository.save(reviewMaster));
        }else {
            AppraisalReviewMaster master = repository.findById(reviewMaster.getId()).orElse(null);
            if (master != null) {
                master.setComment(reviewMaster.getComment());
                master.setRating(reviewMaster.getRating());
                master.setComplete(reviewMaster.isComplete());
                return ResponseEntity.ok(repository.save(master));
            }
        }
        return null;
    }
}
