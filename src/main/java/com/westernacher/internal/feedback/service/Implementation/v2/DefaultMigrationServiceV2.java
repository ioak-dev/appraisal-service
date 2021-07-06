package com.westernacher.internal.feedback.service.Implementation.v2;

import com.westernacher.internal.feedback.domain.*;
import com.westernacher.internal.feedback.domain.v2.*;
import com.westernacher.internal.feedback.repository.*;
import com.westernacher.internal.feedback.repository.v2.AppraisalHeaderRepository;
import com.westernacher.internal.feedback.repository.v2.AppraisalLongRepository;
import com.westernacher.internal.feedback.repository.v2.GoalEmployeeRepository;
import com.westernacher.internal.feedback.service.v2.MigrationServiceV2;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.web.server.ResponseStatusException;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.westernacher.internal.feedback.domain.AppraisalStatusType.REVIEW_GOAL;
import static com.westernacher.internal.feedback.domain.AppraisalStatusType.SET_GOAL;

@Service
@Slf4j
public class DefaultMigrationServiceV2 implements MigrationServiceV2 {

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

    @Autowired
    private AppraisalReviewRepository appraisalReviewRepository;

    @Override
    public void updateCUObjectives() {
        List<AppraisalGoal> appraisalGoalList = appraisalGoalRepository.findAllByJob("");
        int order = 14;
        for (AppraisalGoal appraisalGoal : appraisalGoalList) {
            appraisalGoal.setOrder(order);
            log.info("Appraisal Goal Id and Order " + appraisalGoal.getId() + " " + appraisalGoal.getOrder());
            appraisalGoalRepository.save(appraisalGoal);
            order += 1;
        }
    }


    @Override
    public MigrationOutputV2 getAppraisalData(String cycleId) {
        Map<String, AppraisalHeader> headerMap = new HashMap<>();
        Map<String, List<?>> responseMap = new HashMap<>();
        List<AppraisalHeader> appraisalHeaders = new ArrayList<>();
        List<AppraisalLong> appraisalLongs = new ArrayList<>();
        List<GoalEmployee> goalEmployees = new ArrayList<>();
        Optional<AppraisalCycle> appraisalCycle = appraisalCycleRepository.findById(cycleId);
        List<AppraisalReview> appraisalReviewList;
        if (appraisalCycle.isPresent()) {
            appraisalReviewList = appraisalReviewRepository.findAllByCycleId(appraisalCycle.get().getId());
            List<String> appraisalIdList = appraisalReviewList.stream().map(AppraisalReview::getId).collect(Collectors.toList());
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(appraisalCycle.get().getEnd());
            cal.add(Calendar.DATE, 10);
            List<AppraisalReviewGoal> appraisalReviewGoals = appraisalReviewGoalRepository.findAllByAppraisalIdIn(appraisalIdList);
            appraisalReviewGoals.forEach(appraisalReviewGoal -> {
                Optional<AppraisalGoal> appraisalGoal = appraisalGoalRepository.
                        findById(appraisalReviewGoal.getGoalId());

                if (appraisalReviewGoal.getReviewerType().equals(SET_GOAL.toString())) {
                    if (!appraisalReviewGoal.getComment().isEmpty()) {
                        GoalEmployee goalEmployee = new GoalEmployee();
                        goalEmployee.setId(ObjectId.get().toString());
                        goalEmployee.setEmployeeId(appraisalReviewGoal.getEmployeeId());
                        goalEmployee.setOrderId(appraisalGoal.get().getOrder());
                        goalEmployee.setDescription(appraisalReviewGoal.getComment());
                        goalEmployee.setCreatedDate(appraisalCycle.get().getStart());
                        goalEmployee.setAuditCreateDate(cal.getTime());
                        goalEmployees.add(goalEmployee);
                    }
                } else if (appraisalReviewGoal.getReviewerType().equals(REVIEW_GOAL.toString())) {
                    if (!appraisalReviewGoal.getComment().isEmpty()) {
                        GoalEmployee goalEmployee = new GoalEmployee();
                        goalEmployee.setId(ObjectId.get().toString());
                        goalEmployee.setEmployeeId(appraisalReviewGoal.getEmployeeId());
                        goalEmployee.setOrderId(appraisalGoal.get().getOrder());
                        goalEmployee.setDescription(appraisalReviewGoal.getComment());
                        goalEmployee.setCreatedDate(cal.getTime());
                        goalEmployee.setAuditCreateDate(cal.getTime());
                        goalEmployees.add(goalEmployee);
                    }
                } else {
                    String headerKey = appraisalReviewGoal.getEmployeeId() + "--" + appraisalReviewGoal.getReviewerId() +
                            "--" + appraisalReviewGoal.getReviewerType();
                    String headerId;
                    if (headerMap.containsKey(headerKey))
                        headerId = headerMap.get(headerKey).getId();
                    else {
                        AppraisalHeader appraisalHeader = new AppraisalHeader();
                        appraisalHeader.setId(ObjectId.get().toString());
                        appraisalHeader.setEmployeeId(appraisalReviewGoal.getEmployeeId());
                        appraisalHeader.setReviewerId(appraisalReviewGoal.getReviewerId());
                        appraisalHeader.setReviewerType(appraisalReviewGoal.getReviewerType());
                        appraisalHeader.setFrom(convertDateToInteger(appraisalCycle.get().getStart()));
                        appraisalHeader.setTo(convertDateToInteger(appraisalCycle.get().getEnd()));
                        appraisalHeader.setCreatedDate(cal.getTime());
                        headerMap.put(headerKey, appraisalHeader);
                        headerId = headerMap.get(headerKey).getId();
                    }
                    if (!appraisalReviewGoal.getComment().isEmpty()){
                        AppraisalLong appraisalLong = new AppraisalLong();
                        appraisalLong.setId(ObjectId.get().toString());
                        if (appraisalGoal.get().getJob().isEmpty())
                            log.info("Appraisal Long's goal id and order " + appraisalGoal.get().getId()
                                    + " " + String.valueOf(appraisalGoal.get().getOrder()));
                        appraisalLong.setOrderId(appraisalGoal.get().getOrder());
                        appraisalLong.setComment(appraisalReviewGoal.getComment());
                        if (appraisalReviewGoal.getRating() == null || appraisalReviewGoal.getRating().equals(""))
                            appraisalLong.setRating(0);
                        else
                            appraisalLong.setRating(Integer.parseInt(appraisalReviewGoal.getRating()
                                    .replaceAll("[^0-9]", "")));
                        appraisalLong.setHeaderId(headerId);
                        appraisalLong.setCreatedDate(cal.getTime());
                        appraisalLongs.add(appraisalLong);
                    }
                }

            });
            appraisalHeaders = new ArrayList<>(headerMap.values());
            MigrationOutputV2 migrationOutputV2 = new MigrationOutputV2();
            migrationOutputV2.setAppraisalHeaderMap(Map.ofEntries(Map.entry("appraisal.header", appraisalHeaders)));
            migrationOutputV2.setAppraisalLongMap(Map.ofEntries(Map.entry("appraisal.long", appraisalLongs)));
            migrationOutputV2.setGoalEmployeeMap(Map.ofEntries(Map.entry("goal.employee", goalEmployees)));
            return migrationOutputV2;
        } else
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    private Integer convertDateToInteger(Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        String sMonth;
        if (month < 10) {
            sMonth = "0" + String.valueOf(month);
        } else {
            sMonth = String.valueOf(month);
        }
        return Integer.valueOf(String.valueOf(year) + String.valueOf(sMonth));
    }

    public void loadAppraisalData(MigrationOutputV2 appraisalData) {
        try {
            log.info("Loading appraisal header collection");
            List<AppraisalHeader> appraisalHeaders = appraisalData.getAppraisalHeaderMap().get("appraisal.header");
            appraisalHeaders.forEach(appraisalHeader -> appraisalHeaderRepository.save(appraisalHeader));
            //appraisalHeaderRepository.saveAll(appraisalData.getAppraisalHeaderMap().get("appraisal.header"));
            log.info("Loading appraisal long collection");
            appraisalLongRepository.saveAll(appraisalData.getAppraisalLongMap().get("appraisal.long"));
            log.info("Loading goal employee collection");
            goalEmployeeRepository.saveAll(appraisalData.getGoalEmployeeMap().get("goal.employee"));
        } catch (Exception e) {
            //transactionManager.rollback(TransactionInterceptor.currentTransactionStatus());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public GetAndLoadOutput geMigrationOutputCount() {
        GetAndLoadOutput getAndLoadOutput = new GetAndLoadOutput();
        getAndLoadOutput.setAppraisalHeaderCount(appraisalHeaderRepository.count());
        getAndLoadOutput.setAppraisalLongCount(appraisalLongRepository.count());
        getAndLoadOutput.setGoalEmployeeCount(goalEmployeeRepository.count());
        return getAndLoadOutput;
    }
}
