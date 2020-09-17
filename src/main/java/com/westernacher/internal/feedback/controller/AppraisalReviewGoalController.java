
package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.domain.AppraisalReview;
import com.westernacher.internal.feedback.domain.AppraisalReviewGoal;
import com.westernacher.internal.feedback.domain.AppraisalStatusType;
import com.westernacher.internal.feedback.repository.AppraisalReviewGoalRepository;
import com.westernacher.internal.feedback.repository.AppraisalReviewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.westernacher.internal.feedback.domain.AppraisalStatusType.PROJECT_MANAGER;

@RestController
@RequestMapping("/appraisal/review/goal")
@Slf4j
public class AppraisalReviewGoalController {

    @Autowired
    private AppraisalReviewGoalRepository repository;

    @Autowired
    private AppraisalReviewRepository reviewRepository;

    @GetMapping
    public ResponseEntity<?> getReviewGoals (@RequestParam String appraisalId) {
        List<AppraisalReviewGoal> appraisalReviewGoals = repository.findAllByAppraisalId(appraisalId);
        appraisalReviewGoals.sort(
                Comparator.comparing((AppraisalReviewGoal ARG) -> ARG.getReviewerType().ordinal())
        );
        return ResponseEntity.ok(appraisalReviewGoals);
    }

    @PutMapping
    public ResponseEntity<?> update (@RequestBody List<AppraisalReviewGoal> reviewGoals) {
        List<AppraisalReviewGoal> newReviewGoals = new ArrayList<>();
        reviewGoals.stream().forEach(appraisalReviewGoal -> {
            AppraisalReviewGoal savedReviewGoal = repository.findById(appraisalReviewGoal.getId()).orElse(null);
            if (savedReviewGoal != null) {
                savedReviewGoal.setEmployeeId(appraisalReviewGoal.getEmployeeId() != null ? appraisalReviewGoal.getEmployeeId() : savedReviewGoal.getEmployeeId());
                savedReviewGoal.setAppraisalId(appraisalReviewGoal.getAppraisalId() != null ? appraisalReviewGoal.getAppraisalId() : savedReviewGoal.getAppraisalId());
                savedReviewGoal.setReviewerId(appraisalReviewGoal.getReviewerId());
                savedReviewGoal.setReviewerType(appraisalReviewGoal.getReviewerType());
                savedReviewGoal.setGoalId(appraisalReviewGoal.getGoalId());
                savedReviewGoal.setComment(appraisalReviewGoal.getComment());
                savedReviewGoal.setRating(appraisalReviewGoal.getRating());
                newReviewGoals.add(repository.save(savedReviewGoal));
            }
        });

        return ResponseEntity.ok(newReviewGoals);
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submit (@RequestBody List<AppraisalReviewGoal> reviewGoals) {
        List<AppraisalReviewGoal> newReviewGoals = new ArrayList<>();

        boolean submit = true;
        String appraisalReviewId = null;
        List<String> sectionOneError = new ArrayList<>();
        ErrorResource errorResource = new ErrorResource();

        for (AppraisalReviewGoal appraisalReviewGoal : reviewGoals) {

            if (appraisalReviewGoal.getComment() == null) {
                sectionOneError.add("Null Comment");
                submit = false;
            } /*else if (appraisalReviewGoal.getComment().length()  < 5) {
                sectionOneError.add("Comment length is not less than five");
                submit = false;
            }*/

            if (appraisalReviewGoal.getRating() == null) {
                sectionOneError.add("Rating is null");
                submit = false;
            }

            if (submit == false)  {
                errorResource.setSectionOneError(sectionOneError);
                return new ResponseEntity<ErrorResource>(errorResource, HttpStatus.NOT_ACCEPTABLE);
            } else {
                appraisalReviewGoal.setComplete(true);
                newReviewGoals.add(appraisalReviewGoal);
            }
            appraisalReviewId = appraisalReviewGoal.getAppraisalId();
        }

        if (submit = true) {
            AppraisalReview appraisalReview = reviewRepository.findById(appraisalReviewId).orElse(null);
            if (appraisalReview !=null) {
                if (appraisalReview.getStatus().equals(AppraisalStatusType.SELF_APPRAISAL)) {
                    appraisalReview.setStatus(AppraisalStatusType.PROJECT_MANAGER);
                } else if (appraisalReview.getStatus().equals(AppraisalStatusType.PROJECT_MANAGER)) {
                    appraisalReview.setStatus(AppraisalStatusType.REPORTING_MANAGER);
                } else if (appraisalReview.getStatus().equals(AppraisalStatusType.REPORTING_MANAGER)) {
                    appraisalReview.setStatus(AppraisalStatusType.PRACTICE_DIRECTOR);
                } else if (appraisalReview.getStatus().equals(AppraisalStatusType.PRACTICE_DIRECTOR)) {
                    appraisalReview.setStatus(AppraisalStatusType.HR);
                }
                reviewRepository.save(appraisalReview);
                return ResponseEntity.ok(repository.saveAll(newReviewGoals));
            }
        }
        return null;
    }
}
