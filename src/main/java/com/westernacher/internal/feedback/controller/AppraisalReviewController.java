
package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.repository.AppraisalReviewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/appraisal/review")
@Slf4j
public class AppraisalReviewController {

    @Autowired
    private AppraisalReviewRepository repository;

    @GetMapping
    public ResponseEntity<?> getAllReview () {
        return ResponseEntity.ok(repository.findAll());
    }
}
