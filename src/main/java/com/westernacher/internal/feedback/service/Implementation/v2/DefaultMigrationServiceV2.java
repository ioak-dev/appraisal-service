package com.westernacher.internal.feedback.service.Implementation.v2;

import com.westernacher.internal.feedback.domain.AppraisalCycle;
import com.westernacher.internal.feedback.domain.AppraisalGoal;
import com.westernacher.internal.feedback.domain.AppraisalReviewGoal;
import com.westernacher.internal.feedback.domain.v1Goal;
import com.westernacher.internal.feedback.domain.v2.*;
import com.westernacher.internal.feedback.repository.AppraisalCycleRepository;
import com.westernacher.internal.feedback.repository.AppraisalGoalRepository;
import com.westernacher.internal.feedback.repository.AppraisalReviewGoalRepository;
import com.westernacher.internal.feedback.repository.v1GoalRepository;
import com.westernacher.internal.feedback.repository.v2.AppraisalHeaderRepository;
import com.westernacher.internal.feedback.repository.v2.AppraisalLongRepository;
import com.westernacher.internal.feedback.repository.v2.GoalEmployeeRepository;
import com.westernacher.internal.feedback.service.v2.MigrationServiceV2;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static com.westernacher.internal.feedback.domain.AppraisalStatusType.REVIEW_GOAL;
import static com.westernacher.internal.feedback.domain.AppraisalStatusType.SET_GOAL;

@Service
@Slf4j
public class DefaultMigrationServiceV2 implements MigrationServiceV2 {

    @Autowired
    private v1GoalRepository v1GoalRepository;

    @Autowired
    private AppraisalReviewGoalRepository appraisalReviewGoalRepository;

    @Autowired
    private AppraisalCycleRepository appraisalCycleRepository;

    @Autowired
    private AppraisalGoalRepository appraisalGoalRepository;

    @Autowired
    private AppraisalHeaderRepository appraisalHeaderRepository;

    @Autowired
    private AppraisalLongRepository appraisalLongRepository;

    @Autowired
    private GoalEmployeeRepository goalEmployeeRepository;


    @Override
    public void migratePrerequisiteData(){
        List<v1Goal> v1GoalRepositories = v1GoalRepository.findAll();
        v1GoalRepositories.forEach(v1Goal -> {
            Goal goal = Goal.builder().criteria(v1Goal.getCriteria()).build();
            GoalReference goalReference = GoalReference.builder().description(v1Goal.getDescription())
                    .job(v1Goal.getJob()).build();
        });
    }

    @Override
    public MigrationOutputV2 getAppraisalData(String cycleId){
        Map<String, List<?>> responseMap = new HashMap<>();
        List<AppraisalHeader> appraisalHeaders = new ArrayList<>();
        List<AppraisalLong> appraisalLongs = new ArrayList<>();
        List<GoalEmployee> goalEmployees = new ArrayList<>();
        Optional<AppraisalCycle> appraisalCycle = appraisalCycleRepository.findById(cycleId);
        GregorianCalendar cal = new GregorianCalendar();
        List<AppraisalReviewGoal> appraisalReviewGoals = appraisalReviewGoalRepository.findAll();
        appraisalReviewGoals.forEach(appraisalReviewGoal -> {
            Optional<AppraisalGoal> appraisalGoal = appraisalGoalRepository.
                    findById(appraisalReviewGoal.getGoalId());

            AppraisalHeader appraisalHeader = new AppraisalHeader();
            appraisalHeader.setId(ObjectId.get().toString());
            appraisalHeader.setEmployeeId(appraisalReviewGoal.getEmployeeId());
            appraisalHeader.setReviewerId(appraisalReviewGoal.getReviewerId());
            appraisalHeader.setReviewerType(appraisalReviewGoal.getReviewerType());
            appraisalHeader.setFrom(appraisalCycle.get().getStart());
            appraisalHeader.setTo(appraisalCycle.get().getEnd());
            appraisalHeaders.add(appraisalHeader);

            if (appraisalReviewGoal.getReviewerType().equals(SET_GOAL.toString())){
                GoalEmployee goalEmployee = new GoalEmployee();
                goalEmployee.setId(ObjectId.get().toString());
                goalEmployee.setEmployeeId(appraisalReviewGoal.getEmployeeId());
                goalEmployee.setOrderId(appraisalGoal.get().getOrder());
                goalEmployee.setDescription(appraisalReviewGoal.getComment());
                goalEmployee.setCreatedDate(appraisalCycle.get().getStart());
                goalEmployees.add(goalEmployee);
            }
            else if(appraisalReviewGoal.getReviewerType().equals(REVIEW_GOAL.toString())){
                GoalEmployee goalEmployee = new GoalEmployee();
                goalEmployee.setId(ObjectId.get().toString());
                goalEmployee.setEmployeeId(appraisalReviewGoal.getEmployeeId());
                goalEmployee.setOrderId(appraisalGoal.get().getOrder());
                goalEmployee.setDescription(appraisalReviewGoal.getComment());
                cal.setTime(appraisalCycle.get().getEnd());
                cal.add(Calendar.DATE, 10);
                goalEmployee.setCreatedDate(cal.getTime());
                goalEmployees.add(goalEmployee);
            }
            else{
                AppraisalLong appraisalLong = new AppraisalLong();
                appraisalLong.setId(ObjectId.get().toString());
                appraisalLong.setOrderId(appraisalGoal.get().getOrder());
                appraisalLong.setComment(appraisalReviewGoal.getComment());
                appraisalLong.setRating(Integer.parseInt(appraisalReviewGoal.getRating()
                        .replaceAll("[^0-9]", "")));
                appraisalLong.setHeaderId(appraisalHeader.getId());
                appraisalLongs.add(appraisalLong);
            }
        });
        MigrationOutputV2 migrationOutputV2 = new MigrationOutputV2();
        migrationOutputV2.setAppraisalHeaderMap(Map.ofEntries(Map.entry("appraisal.header", appraisalHeaders)));
        migrationOutputV2.setAppraisalLongMap(Map.ofEntries(Map.entry("appraisal.long", appraisalLongs)));
        migrationOutputV2.setGoalEmployeeMap(Map.ofEntries(Map.entry("goal.employee", goalEmployees)));
        return migrationOutputV2;
    }

    public void loadAppraisalData(MigrationOutputV2 appraisalData){
        try{
            appraisalHeaderRepository.saveAll(appraisalData.getAppraisalHeaderMap().get("appraisal.header"));
            appraisalLongRepository.saveAll(appraisalData.getAppraisalLongMap().get("appraisal.long"));
            goalEmployeeRepository.saveAll(appraisalData.getGoalEmployeeMap().get("goal.employee"));
        } catch (Exception e){
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

    }
}
