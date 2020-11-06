package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.controller.representation.MigrationAppraisalPayload;
import com.westernacher.internal.feedback.domain.*;
import com.westernacher.internal.feedback.repository.AppraisalGoalRepository;
import com.westernacher.internal.feedback.repository.AppraisalReviewGoalRepository;
import com.westernacher.internal.feedback.repository.AppraisalReviewRepository;
import com.westernacher.internal.feedback.service.MigrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/migrate")
public class MigrationController {

    @Autowired
    private MigrationService service;

    @Autowired
    private AppraisalGoalRepository appraisalGoalRepository;

    @Autowired
    private AppraisalReviewRepository appraisalReviewRepository;

    @Autowired
    private AppraisalReviewGoalRepository appraisalReviewGoalRepository;

    @PostMapping
    public ResponseEntity<?> migrateAppraisal (@RequestParam String cycleId,
                                               @RequestBody MigrationAppraisalPayload payload) {
        return ResponseEntity.ok(service.migrate(cycleId, payload.getAppraisals(), service.getPersonMap(payload.getPersons()), payload.getGoalOrder()));
    }

    @PostMapping("/new")
    public void createNewAppraisalData(@RequestBody MigrationOutput output) {
        service.migrateToNewDb(output);
    }

    @PostMapping("/{sourceCycle}/{destinationCycle}")
    public void createNewAppraisalData(@PathVariable String sourceCycle,
                                       @PathVariable String destinationCycle) {

        //List<appraisal.goal> by sourceCycle => Map<goalId, "replacespaces(lower(group))::replacespaces(lower(criteria))"> sourceGoalReferenceMap

        Map<String, String> sourceGoalReferenceMap = new HashMap();
        List<AppraisalGoal> appraisalGoalList = appraisalGoalRepository.findAllByCycleId(sourceCycle);
        appraisalGoalList.stream().forEach(appraisalGoal -> {
            sourceGoalReferenceMap.put(appraisalGoal.getId(), appraisalGoal.getGroup().toLowerCase().replaceAll("\\s", "")+
                    "::"+appraisalGoal.getCriteria().toLowerCase().replaceAll("\\s", ""));
        });

        //List<appraisal.review> by sourceCycle => List<_id> sourceAppraisalIdList
        List<String> sourceAppraisalIdList = new ArrayList();
        List<AppraisalReview> sourceAppraisalList = appraisalReviewRepository.findAllByCycleId(sourceCycle);
        sourceAppraisalList.stream().forEach(appraisalReview -> {
            sourceAppraisalIdList.add(appraisalReview.getId());
        });


        //List<appraisal.review.goal> by [appraisalId in sourceAppraisalIdList] and [reviewerType = Set_Goal] => Map<employeeId::sourceGoalReferenceMap.get(goalId), appraisal.review.goal> sourceSetGoalsMap
        List<AppraisalReviewGoal> appraisalReviewGoalLists = appraisalReviewGoalRepository.findAllByAppraisalIdInAndReviewerType(sourceAppraisalIdList, AppraisalStatusType.SET_GOAL);
        Map<String, AppraisalReviewGoal> sourceSetGoalsMap = new HashMap<>();
        appraisalReviewGoalLists.stream().forEach(appraisalReviewGoal -> {
            sourceSetGoalsMap.put(appraisalReviewGoal.getEmployeeId()+"::"+sourceGoalReferenceMap.get(appraisalReviewGoal.getGoalId()), appraisalReviewGoal);
        });

        //List<appraisal.goal> by destCycle => Map<"replacespaces(lower(group))::replacespaces(lower(criteria))", goalId> destGoalReferenceMap
        Map<String, String> destGoalReferenceMap = new HashMap();
        List<AppraisalGoal> appraisalGoalListDest = appraisalGoalRepository.findAllByCycleId(destinationCycle);
        appraisalGoalListDest.stream().forEach(appraisalGoal -> {
            destGoalReferenceMap.put(appraisalGoal.getGroup().toLowerCase().replaceAll("\\s", "")+
                    "::"+appraisalGoal.getCriteria().toLowerCase().replaceAll("\\s", ""), appraisalGoal.getId());
        });

        //List<appraisal.review> by destCycle => Map<employeeId, _id> destEmployeeIdToAppraisalIdMap
        Map<String, String> destEmployeeIdToAppraisalIdMap = new HashMap<>();
        List<AppraisalReview> destAppraisalList = appraisalReviewRepository.findAllByCycleId(destinationCycle);
        sourceAppraisalList.stream().forEach(appraisalReview -> {
            destEmployeeIdToAppraisalIdMap.put(appraisalReview.getEmployeeId(), appraisalReview.getId());
        });

        //appraisalReviewGoalList = new ArrayList()
        List<AppraisalReviewGoal> appraisalReviewGoalList = new ArrayList<>();

        //sourceSetGoalsMap.keyset() -> iterate for each setGoal
        //- setGoal.tokenize("::") => String[employeeId, group, criteria] => employeeId = [0]; goalKey = [1]::[2]
        //- goalId = destGoalReferenceMap.get(goalKey)
          //      - appraisalId = destEmployeeIdToAppraisalIdMap.get(employeeId)
            //    - appraisalReviewGoal = Create a new appraisal.review.goal object using all fields from setGoal, except for the below fields
              //  -- _id: don't copy
             //   -- appraisalId = use appraisalId computed before
              //  -- goalId = use goalId computed before
               // -- reviewerType = AppraisalStatus.Review_Goal
                //- appraisalReviewGoalList.add(appraisalReviewGoal)
        sourceSetGoalsMap.keySet().stream().forEach(p -> {
            String[] splitsValue = p.split("::");
            String employeeId = splitsValue[0];
            String goalKey = splitsValue[1]+"::"+splitsValue[2];
            String goalId = destGoalReferenceMap.get(goalKey);
            String appraisalId = destEmployeeIdToAppraisalIdMap.get(employeeId);
            AppraisalReviewGoal appraisalReviewGoal = new AppraisalReviewGoal();
            appraisalReviewGoal.setEmployeeId(sourceSetGoalsMap.get(p).getEmployeeId());
            appraisalReviewGoal.setAppraisalId(appraisalId);
            appraisalReviewGoal.setReviewerType(AppraisalStatusType.REVIEW_GOAL);
            appraisalReviewGoal.setGoalId(goalId);
            appraisalReviewGoal.setComment(sourceSetGoalsMap.get(p).getComment());
            appraisalReviewGoal.setRating(sourceSetGoalsMap.get(p).getRating());
            appraisalReviewGoal.setComplete(sourceSetGoalsMap.get(p).isComplete());
            appraisalReviewGoal.setScore(sourceSetGoalsMap.get(p).getScore());
            appraisalReviewGoalList.add(appraisalReviewGoal);
        });


        //persiste appraisalReviewGoalList to db
        appraisalReviewGoalRepository.saveAll(appraisalReviewGoalList);
    }
}


