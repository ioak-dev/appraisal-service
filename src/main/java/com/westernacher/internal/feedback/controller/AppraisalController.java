package com.westernacher.internal.feedback.controller;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.westernacher.internal.feedback.controller.representation.ReviewResource;
import com.westernacher.internal.feedback.domain.*;
import com.westernacher.internal.feedback.repository.AppraisalCycleRepository;
import com.westernacher.internal.feedback.repository.AppraisalRepository;
import com.westernacher.internal.feedback.repository.PersonRepository;
import com.westernacher.internal.feedback.service.AppraisalService;
import com.westernacher.internal.feedback.service.CsvObject;
import com.westernacher.internal.feedback.service.EmailUtility;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/appraisal")
@Slf4j
public class AppraisalController {

    @Autowired
    private AppraisalRepository repository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private AppraisalCycleRepository appraisalCycleRepository;

    @Autowired
    private AppraisalService service;

    @RequestMapping(value = "/cycle/{id}", method = RequestMethod.GET)
    public List<Appraisal> getAllByCycle (@PathVariable("id") String id) {
        return repository.findAllByCycleId(id);
    }

    @RequestMapping(value = "/cycle/{cycleId}/manageable/{userId}", method = RequestMethod.GET)
    public List<Appraisal> getAllByCycleManageable (@PathVariable("cycleId") String cycleId, @PathVariable("userId") String userId) {

        List<String> emailIdList = new ArrayList<>();
        personRepository.findById(userId).orElse(null).getRoles().stream().forEach(role -> {
            emailIdList.addAll(role.getOptions());
        });

        List<String> userIdList = new ArrayList<>();

        personRepository.findByEmailIn(emailIdList).stream().forEach(person -> {
            userIdList.add(person.getId());
        });

        return repository.findAllByCycleIdAndUserIdIsIn(cycleId, userIdList);
    }

    @RequestMapping(value = "/cycle/{id}/user/{userId}", method = RequestMethod.GET)
    public Appraisal getAllByCycleAndUser (@PathVariable("id") String id, @PathVariable("userId") String userId) {
        return repository.findOneByCycleIdAndUserId(id, userId);
    }

    @RequestMapping(value = "/cycle/{id}/user/{userId}/sectionone", method = RequestMethod.GET)
    public List<ObjectiveResponseGroup> getSectionOneByCycleAndUser (@PathVariable("id") String id, @PathVariable("userId") String userId) {
        return repository.findOneByCycleIdAndUserId(id, userId).getSectiononeResponse();
    }

    @RequestMapping(value = "/cycle/{id}/user/{userId}/sectionone", method = RequestMethod.PUT)
    public void saveSectionOne (@PathVariable("id") String id, @PathVariable("userId") String userId,
                                @RequestBody List<ObjectiveResponseGroup> sectionone) {
        Appraisal appraisal = repository.findOneByCycleIdAndUserId(id, userId);
        appraisal.setSectiononeResponse(sectionone);
        repository.save(appraisal);
    }

    @RequestMapping(value = "/cycle/{id}/user/{userId}/sectionone/reviewer/{reviewerId}", method = RequestMethod.PUT)
    public void saveSectionOne (@PathVariable("id") String id, @PathVariable("userId") String userId, @PathVariable("reviewerId") String reviewerId,
                                @RequestBody List<ReviewResource> reviewResourceList) {
        Appraisal appraisal = repository.findOneByCycleIdAndUserId(id, userId);
        Map<String, Map<String, ObjectiveResponse>> sourceMap = new HashMap<>();
        appraisal.getSectiononeResponse().forEach(group -> {
            Map<String, ObjectiveResponse> criteriaMap = new HashMap<>();
            group.getResponse().forEach(criteria -> {
                criteriaMap.put(criteria.getCriteria(), criteria);
            });
            sourceMap.put(group.getGroup(), criteriaMap);
        });
        reviewResourceList.forEach(item -> {
            ReviewerElements elements = ReviewerElements
                    .builder()
                    .rating(item.getRating())
                    .comment(item.getComment())
                    .name(personRepository.findPersonById(item.getReviewerId()).getName())
                    .build();
            if (item.getRoleType().equals(RoleType.ProjectManager)) {
                sourceMap.get(item.getGroup()).get(item.getCriteria()).getProjectManagerReviews().put(item.getReviewerId(), elements);
            } else if (item.getRoleType().equals(RoleType.TeamLead)) {
                sourceMap.get(item.getGroup()).get(item.getCriteria()).getTeamLeadReviews().put(item.getReviewerId(), elements);
            } else if (item.getRoleType().equals(RoleType.PracticeDirector)) {
                sourceMap.get(item.getGroup()).get(item.getCriteria()).getPracticeDirectorReviews().put(item.getReviewerId(), elements);
            } else if (item.getRoleType().equals(RoleType.HR)) {
                sourceMap.get(item.getGroup()).get(item.getCriteria()).getHrReviews().put(item.getReviewerId(), elements);
            }
        });
        repository.save(appraisal);
    }

    @RequestMapping(value = "/{appraisalId}/sectionone/reviewer/{reviewerId}/submit", method = RequestMethod.POST)
    public void submitSectionOneByAppraisalID (@PathVariable("appraisalId") String appraisalId, @PathVariable("reviewerId") String reviewerId) {
        Appraisal appraisal = repository.findById(appraisalId).orElse(null);

        Map<String, Map<String, ObjectiveResponse>> sourceMap = new HashMap<>();
        appraisal.getSectiononeResponse().forEach(group -> {
            Map<String, ObjectiveResponse> criteriaMap = new HashMap<>();
            group.getResponse().forEach(criteria -> {

                if (criteria.getProjectManagerReviews().containsKey(reviewerId))  {
                    boolean flag = true;
                    ReviewerElements reviewerElements = criteria.getProjectManagerReviews().get(reviewerId);
                    reviewerElements.setComplete(true);
                    criteria.getProjectManagerReviews().put(reviewerId, reviewerElements);

                    for (Map.Entry<String, ReviewerElements> entry : criteria.getProjectManagerReviews().entrySet()){
                        if (entry.getValue().isComplete() == false){
                            flag = false;
                        }
                    }
                    if (flag == true) {
                        appraisal.setStatus(AppraisalStatusType.REPORTING_MANAGER);

                        if (criteria.getTeamLeadReviews().isEmpty()){
                            appraisal.setStatus(AppraisalStatusType.PRACTICE_DIRECTOR);
                            if (criteria.getPracticeDirectorReviews().isEmpty()){
                                appraisal.setStatus(AppraisalStatusType.HR);
                            }
                        }

                        if (appraisal.getStatus().equals(AppraisalStatusType.REPORTING_MANAGER)) {
                            service.sendListOfMail(criteria.getTeamLeadReviews().keySet(), AppraisalStatusType.REPORTING_MANAGER, appraisal.getUserId());
                        }else if (appraisal.getStatus().equals(AppraisalStatusType.PRACTICE_DIRECTOR)) {
                            service.sendListOfMail(criteria.getPracticeDirectorReviews().keySet(), AppraisalStatusType.PRACTICE_DIRECTOR, appraisal.getUserId());
                        }else if (appraisal.getStatus().equals(AppraisalStatusType.HR)) {
                            service.sendListOfMail(criteria.getHrReviews().keySet(), AppraisalStatusType.HR, appraisal.getUserId());
                        }
                    }
                }
                if (criteria.getTeamLeadReviews().containsKey(reviewerId))  {
                    boolean flag = true;

                    ReviewerElements reviewerElements = criteria.getTeamLeadReviews().get(reviewerId);
                    reviewerElements.setComplete(true);
                    criteria.getTeamLeadReviews().put(reviewerId, reviewerElements);

                    for (Map.Entry<String, ReviewerElements> entry : criteria.getTeamLeadReviews().entrySet()){
                        if (entry.getValue().isComplete() == false){
                            flag = false;
                        }
                    }
                    if (flag == true) {
                        appraisal.setStatus(AppraisalStatusType.PRACTICE_DIRECTOR);

                        if (criteria.getPracticeDirectorReviews().isEmpty()){
                            appraisal.setStatus(AppraisalStatusType.HR);
                        }

                        if (appraisal.getStatus().equals(AppraisalStatusType.PRACTICE_DIRECTOR)) {
                            service.sendListOfMail(criteria.getPracticeDirectorReviews().keySet(), AppraisalStatusType.PRACTICE_DIRECTOR, appraisal.getUserId());
                        }else if (appraisal.getStatus().equals(AppraisalStatusType.HR)) {
                            service.sendListOfMail(criteria.getHrReviews().keySet(), AppraisalStatusType.HR, appraisal.getUserId());
                        }
                    }

                }
                if (criteria.getPracticeDirectorReviews().containsKey(reviewerId))  {
                    boolean flag = true;

                    ReviewerElements reviewerElements = criteria.getPracticeDirectorReviews().get(reviewerId);
                    reviewerElements.setComplete(true);
                    criteria.getPracticeDirectorReviews().put(reviewerId, reviewerElements);

                    for (Map.Entry<String, ReviewerElements> entry : criteria.getPracticeDirectorReviews().entrySet()){
                        if (entry.getValue().isComplete() == false){
                            flag = false;
                        }
                    }
                    if (flag == true) {
                        appraisal.setStatus(AppraisalStatusType.HR);
                        service.sendListOfMail(criteria.getHrReviews().keySet(), AppraisalStatusType.HR, appraisal.getUserId());
                    }
                }
                if (criteria.getHrReviews().containsKey(reviewerId))  {
                    boolean flag = true;

                    ReviewerElements reviewerElements = criteria.getHrReviews().get(reviewerId);
                    reviewerElements.setComplete(true);
                    criteria.getHrReviews().put(reviewerId, reviewerElements);

                    for (Map.Entry<String, ReviewerElements> entry : criteria.getHrReviews().entrySet()){
                        if (entry.getValue().isComplete() == false){
                            flag = false;
                        }
                    }
                    if (flag == true) {
                        appraisal.setStatus(AppraisalStatusType.COMPLETE);
                    }
                }
            });
            sourceMap.put(group.getGroup(), criteriaMap);
        });

        repository.save(appraisal);
    }

    @RequestMapping(value = "/cycle/{id}/user/{userId}/sectiontwo", method = RequestMethod.GET)
    public List<SubjectiveResponse> getSectionTwoByCycleAndUser (@PathVariable("id") String id, @PathVariable("userId") String userId) {
        return repository.findOneByCycleIdAndUserId(id, userId).getSectiontwoResponse();
    }

    @RequestMapping(value = "/cycle/{id}/user/{userId}/sectiontwo", method = RequestMethod.PUT)
    public void saveSectionTwo (@PathVariable("id") String id, @PathVariable("userId") String userId,
                                @RequestBody List<SubjectiveResponse> sectiontwo) {
        Appraisal appraisal = repository.findOneByCycleIdAndUserId(id, userId);
        appraisal.setSectiontwoResponse(sectiontwo);

        if (appraisal.getSectiontwoResponse()!=null) {
            List<SubjectiveResponse> nonEmptySectiontwoResponse= appraisal.getSectiontwoResponse().stream().filter(subjectiveResponse ->
                    ((subjectiveResponse.getComment()!=null && !subjectiveResponse.getComment().isEmpty())
                            ||(subjectiveResponse.getDuration()!=null && !subjectiveResponse.getDuration().isEmpty())
                            ||(subjectiveResponse.getTopic()!=null && !subjectiveResponse.getTopic().isEmpty())))
                    .collect(Collectors.toList());

            appraisal.setSectiontwoResponse(nonEmptySectiontwoResponse);
        }
        repository.save(appraisal);
    }

    @RequestMapping(value = "/cycle/{id}/user/{userId}/sectionthree", method = RequestMethod.GET)
    public List<SubjectiveResponse> getSectionThreeoByCycleAndUser (@PathVariable("id") String id, @PathVariable("userId") String userId) {
        return repository.findOneByCycleIdAndUserId(id, userId).getSectionthreeResponse();
    }

    @RequestMapping(value = "/cycle/{id}/user/{userId}/sectionthree", method = RequestMethod.PUT)
    public void saveSectionThree (@PathVariable("id") String id, @PathVariable("userId") String userId,
                                  @RequestBody List<SubjectiveResponse> sectionthree) {
        Appraisal appraisal = repository.findOneByCycleIdAndUserId(id, userId);
        appraisal.setSectionthreeResponse(sectionthree);

        if (appraisal.getSectionthreeResponse()!=null) {
            List<SubjectiveResponse> nonEmptySectionthreeResponse = appraisal.getSectionthreeResponse().stream().filter(subjectiveResponse ->
                    ((subjectiveResponse.getComment()!=null && !subjectiveResponse.getComment().isEmpty())
                            ||(subjectiveResponse.getDuration()!=null && !subjectiveResponse.getDuration().isEmpty())
                            ||(subjectiveResponse.getTopic()!=null && !subjectiveResponse.getTopic().isEmpty())))
                    .collect(Collectors.toList());

            appraisal.setSectionthreeResponse(nonEmptySectionthreeResponse);
        }

        repository.save(appraisal);
    }

    @RequestMapping(value = "/cycle/{id}/user/{userId}/sectionfour", method = RequestMethod.GET)
    public String getSectionFourByCycleAndUser (@PathVariable("id") String id, @PathVariable("userId") String userId) {
        return repository.findOneByCycleIdAndUserId(id, userId).getSectionfourResponse();
    }

    @RequestMapping(value = "/cycle/{id}/user/{userId}/sectionfour", method = RequestMethod.PUT)
    public void saveSectionFour (@PathVariable("id") String id, @PathVariable("userId") String userId,
                                 @RequestBody String sectionfour) {
        Appraisal appraisal = repository.findOneByCycleIdAndUserId(id, userId);
        appraisal.setSectionfourResponse(sectionfour);
        repository.save(appraisal);
    }

    @RequestMapping(value = "/cycle/{id}/user/{userId}/sectionfive", method = RequestMethod.GET)
    public String getSectionFiveByCycleAndUser (@PathVariable("id") String id, @PathVariable("userId") String userId) {
        return repository.findOneByCycleIdAndUserId(id, userId).getSectionfiveResponse();
    }

    @RequestMapping(value = "/cycle/{id}/user/{userId}/sectionfive", method = RequestMethod.PUT)
    public void saveSectionFive (@PathVariable("id") String id, @PathVariable("userId") String userId,
                                 @RequestBody String sectionfive) {
        Appraisal appraisal = repository.findOneByCycleIdAndUserId(id, userId);
        appraisal.setSectionfiveResponse(sectionfive);
        repository.save(appraisal);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Appraisal get (@PathVariable("id") String id) {
        return repository.findById(id).orElse(null);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete (@PathVariable("id") String id) {
        repository.deleteById(id);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public void saveAppraisal(@Valid @RequestBody Appraisal appraisal) {

        if (appraisal.getSectiontwoResponse()!=null) {
            List<SubjectiveResponse> nonEmptySectiontwoResponse= appraisal.getSectiontwoResponse().stream().filter(subjectiveResponse ->
                    ((subjectiveResponse.getComment()!=null && !subjectiveResponse.getComment().isEmpty())
                            ||(subjectiveResponse.getDuration()!=null && !subjectiveResponse.getDuration().isEmpty())
                            ||(subjectiveResponse.getTopic()!=null && !subjectiveResponse.getTopic().isEmpty())))
                    .collect(Collectors.toList());

            appraisal.setSectiontwoResponse(nonEmptySectiontwoResponse);
        }

        if (appraisal.getSectionthreeResponse()!=null) {
            List<SubjectiveResponse> nonEmptySectionthreeResponse = appraisal.getSectionthreeResponse().stream().filter(subjectiveResponse ->
                    ((subjectiveResponse.getComment()!=null && !subjectiveResponse.getComment().isEmpty())
                            ||(subjectiveResponse.getDuration()!=null && !subjectiveResponse.getDuration().isEmpty())
                            ||(subjectiveResponse.getTopic()!=null && !subjectiveResponse.getTopic().isEmpty())))
                    .collect(Collectors.toList());

            appraisal.setSectionthreeResponse(nonEmptySectionthreeResponse);
        }

        repository.save(appraisal);
    }

    @RequestMapping(value = "/{id}/submitSelfAppraisal", method = RequestMethod.POST)
    public ResponseEntity<ErrorResource> submitSelfAppraisal(@PathVariable("id") String id) {

        Appraisal appraisal = repository.findById(id).orElse(null);


        List<String> sectionOneError = new ArrayList<>();
        ErrorResource errorResource = new ErrorResource();

        /*Validation for section One*/
        if (appraisal!=null) {
            List<ObjectiveResponseGroup> objectiveResponseGroups = appraisal.getSectiononeResponse();

            objectiveResponseGroups.stream().forEach(objectiveResponseGroup -> {
                objectiveResponseGroup.getResponse().stream().forEach(objectiveResponse -> {
                    if (Math.signum(objectiveResponse.getWeightage()) != 0) {
                        if (objectiveResponse.getSelfComment()==null) {
                            sectionOneError.add(objectiveResponseGroup.getGroup()+" > "+objectiveResponse.getCriteria()+" > Comment");
                        } else if (objectiveResponse.getSelfComment().length()<50) {
                            sectionOneError.add(objectiveResponseGroup.getGroup()+" > "+objectiveResponse.getCriteria()+" > Comment should be atleast 50 characters");
                        }
                        if (objectiveResponse.getSelfRating()==null) {
                            sectionOneError.add(objectiveResponseGroup.getGroup()+" > "+objectiveResponse.getCriteria()+" > Rating");
                        }
                    }
                });
                errorResource.setSectionOneError(sectionOneError);
            });
        }

        if (errorResource.getSectionOneError().size()>0) {
            return new ResponseEntity<ErrorResource>(errorResource, HttpStatus.NOT_ACCEPTABLE);
        }else {
            if (appraisal.getSectiononeResponse().get(0).getResponse().get(0).getProjectManagerReviews().isEmpty()){
                appraisal.setStatus(AppraisalStatusType.REPORTING_MANAGER);
                if (appraisal.getSectiononeResponse().get(0).getResponse().get(0).getTeamLeadReviews().isEmpty()){
                    appraisal.setStatus(AppraisalStatusType.PRACTICE_DIRECTOR);
                    if (appraisal.getSectiononeResponse().get(0).getResponse().get(0).getPracticeDirectorReviews().isEmpty()){
                        appraisal.setStatus(AppraisalStatusType.HR);
                    }
                }
            } else {
                appraisal.setStatus(AppraisalStatusType.PROJECT_MANAGER);
                service.sendListOfMail(appraisal.getSectiononeResponse().get(0).getResponse().get(0).getProjectManagerReviews().keySet(), AppraisalStatusType.PROJECT_MANAGER, appraisal.getUserId());
            }
            if (appraisal.getStatus().equals(AppraisalStatusType.REPORTING_MANAGER)) {
                service.sendListOfMail(appraisal.getSectiononeResponse().get(0).getResponse().get(0).getTeamLeadReviews().keySet(), AppraisalStatusType.REPORTING_MANAGER, appraisal.getUserId());
            }else if (appraisal.getStatus().equals(AppraisalStatusType.PRACTICE_DIRECTOR)) {
                service.sendListOfMail(appraisal.getSectiononeResponse().get(0).getResponse().get(0).getPracticeDirectorReviews().keySet(), AppraisalStatusType.PRACTICE_DIRECTOR, appraisal.getUserId());
            }else if (appraisal.getStatus().equals(AppraisalStatusType.HR)) {
                service.sendListOfMail(appraisal.getSectiononeResponse().get(0).getResponse().get(0).getHrReviews().keySet(), AppraisalStatusType.HR, appraisal.getUserId());
            }
            repository.save(appraisal);

            return new ResponseEntity<ErrorResource>(errorResource, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/{id}/submitSelfGoals", method = RequestMethod.POST)
    public ResponseEntity<?> submitSelfGoals(@PathVariable("id") String id) {
        Appraisal appraisal = repository.findById(id).orElse(null);
        appraisal.setStatus(AppraisalStatusType.SELF_REVIEW);
        repository.save(appraisal);
        return new ResponseEntity<>("success", HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/submitReviewerAppraisal", method = RequestMethod.POST)
    public ResponseEntity<ErrorResource> submitReviewerAppraisal(@PathVariable("id") String id) {

        Appraisal appraisal = repository.findById(id).orElse(null);


        List<String> sectionOneError = new ArrayList<>();
        ErrorResource errorResource = new ErrorResource();

        /*Validation for section One*/
        if (appraisal!=null) {
            List<ObjectiveResponseGroup> objectiveResponseGroups = appraisal.getSectiononeResponse();

            objectiveResponseGroups.stream().forEach(objectiveResponseGroup -> {
                objectiveResponseGroup.getResponse().stream().forEach(objectiveResponse -> {
                    /* 20202020 */
//                    if (objectiveResponse.getReviewerRating()==null) {
//                        sectionOneError.add(objectiveResponseGroup.getGroup()+" > "+objectiveResponse.getCriteria()+" > Rating");
//                    }
                });
                errorResource.setSectionOneError(sectionOneError);
            });
        }

        if (errorResource.getSectionOneError().size()>0) {
            return new ResponseEntity<ErrorResource>(errorResource, HttpStatus.NOT_ACCEPTABLE);
        }else {
            appraisal.setStatus(AppraisalStatusType.SCHEDULED);
            repository.save(appraisal);
            return new ResponseEntity<ErrorResource>(errorResource, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/{id}/errorCheck", method = RequestMethod.POST)
    public ResponseEntity<ErrorResource> errorCheck(@PathVariable("id") String id) {

        Appraisal appraisal = repository.findById(id).orElse(null);


        List<String> sectionOneError = new ArrayList<>();
        ErrorResource errorResource = new ErrorResource();

        /*Validation for section One*/
        if (appraisal!=null) {
            List<ObjectiveResponseGroup> objectiveResponseGroups = appraisal.getSectiononeResponse();

            objectiveResponseGroups.stream().forEach(objectiveResponseGroup -> {
                objectiveResponseGroup.getResponse().stream().forEach(objectiveResponse -> {
                    if (Math.signum(objectiveResponse.getWeightage()) != 0) {
                        if (objectiveResponse.getSelfComment()==null) {
                            sectionOneError.add(objectiveResponseGroup.getGroup()+" > "+objectiveResponse.getCriteria()+" > Comment");
                        } else if (objectiveResponse.getSelfComment().length()<50) {
                            sectionOneError.add(objectiveResponseGroup.getGroup()+" > "+objectiveResponse.getCriteria()+" > Comment should be atleast 50 characters");
                        }
                        if (objectiveResponse.getSelfRating()==null) {
                            sectionOneError.add(objectiveResponseGroup.getGroup()+" > "+objectiveResponse.getCriteria()+" > Rating");
                        }
                    }
                });
                errorResource.setSectionOneError(sectionOneError);
            });
        }

        if (errorResource.getSectionOneError().size()>0) {
            return new ResponseEntity<ErrorResource>(errorResource, HttpStatus.NOT_ACCEPTABLE);
        }else {
            return new ResponseEntity<ErrorResource>(errorResource, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/{id}/completeAppraisal", method = RequestMethod.POST)
    public void completeAppraisal(@PathVariable("id") String id) {
        Appraisal appraisal = repository.findById(id).orElse(null);
        appraisal.setStatus(AppraisalStatusType.COMPLETE);
        repository.save(appraisal);
    }

    @RequestMapping(value = "/getStatusCount", method = RequestMethod.GET)
    public StatusResource getAllByCycle () {
        String cycleId = null;

        List<AppraisalCycle> appraisalCycles = appraisalCycleRepository.findAll();

        for (AppraisalCycle appraisalCycle:appraisalCycles) {
            if (appraisalCycle.getStatus()==AppraisalCycleStatusType.ACTIVE) {
                cycleId = appraisalCycle.getId();
            }
        }

        if (cycleId==null) {
            Date date = null;
            for (AppraisalCycle appraisalCycle:appraisalCycles) {
                cycleId = appraisalCycle.getId();
                date = appraisalCycle.getStartDate();
            }
            for (AppraisalCycle appraisalCycle:appraisalCycles) {
                if (appraisalCycle.getStartDate().compareTo(date)>0)  {
                    date = appraisalCycle.getStartDate();
                    cycleId = appraisalCycle.getId();
                }
            }
        }

        List<Appraisal> appraisals = repository.findAllByCycleId(cycleId);
        int selfReview=0;
        int headReview=0;
        int scheduled=0;
        int complete=0;
        for (Appraisal appraisal:appraisals) {
            if (appraisal.getStatus()==AppraisalStatusType.SELF_REVIEW) {
                selfReview++;
            }else if (appraisal.getStatus()==AppraisalStatusType.HEAD_REVIEW) {
                headReview++;
            }else if (appraisal.getStatus()==AppraisalStatusType.SCHEDULED) {
                scheduled++;
            }else if (appraisal.getStatus()==AppraisalStatusType.COMPLETE) {
                complete++;
            }
        }
        StatusResource resource = new StatusResource();
        resource.setSelfReview(String.valueOf(selfReview));
        resource.setHeadReview(String.valueOf(headReview));
        resource.setScheduled(String.valueOf(scheduled));
        resource.setComplete(String.valueOf(complete));

        return resource;
    }

    @RequestMapping(value = "/{cycleName}/appraisalSectionOne", method = RequestMethod.GET)
    public StringBuffer getAppraisalSectionOne (@PathVariable("cycleName") String cycleName) {
        AppraisalCycle appraisalCycle = appraisalCycleRepository.findByName(cycleName);
        List<Appraisal> appraisals = repository.findAllByCycleId(appraisalCycle.getId());
        StringBuffer sectiononeContent = new StringBuffer();

        for (Appraisal appraisal:appraisals){
            List<ObjectiveResponseGroup> sectiononeResponses = appraisal.getSectiononeResponse();
            for (ObjectiveResponseGroup objectiveResponseGroup:sectiononeResponses){
                for (ObjectiveResponse objectiveResponse:objectiveResponseGroup.getResponse()) {
                    sectiononeContent.append(objectiveResponseGroup.getGroup());
                    sectiononeContent.append(",");
                    sectiononeContent.append(getCommaSeparatedString(objectiveResponse));
                    sectiononeContent.append('\n');
                }
            }
        }

        return sectiononeContent;
    }

    private StringBuffer getCommaSeparatedString(ObjectiveResponse objectiveResponse) {
        StringBuffer objectiveResponseContent = new StringBuffer();
        objectiveResponseContent.append(objectiveResponse.getCriteria());
        objectiveResponseContent.append(",");
        objectiveResponseContent.append(objectiveResponse.getWeightage());
        objectiveResponseContent.append(",");
        objectiveResponseContent.append(objectiveResponse.getSelfComment());
        objectiveResponseContent.append(",");
        objectiveResponseContent.append(objectiveResponse.getSelfRating());
        /* 20202020 */
//        objectiveResponseContent.append(",");
//        objectiveResponseContent.append(objectiveResponse.getReviewerComment());
//        objectiveResponseContent.append(",");
//        objectiveResponseContent.append(objectiveResponse.getReviewerRating());
        return objectiveResponseContent;
    }

    @RequestMapping(value = "/{cycleName}/appraisalSectionTwo", method = RequestMethod.GET)
    public StringBuffer getAppraisalSectionTwo (@PathVariable("cycleName") String cycleName) {

        AppraisalCycle appraisalCycle = appraisalCycleRepository.findByName(cycleName);
        List<Appraisal> appraisals = repository.findAllByCycleId(appraisalCycle.getId());
        StringBuffer sectionTwoContent = new StringBuffer();

        for (Appraisal appraisal:appraisals) {
            List<SubjectiveResponse> sectionTwoResponses = appraisal.getSectiontwoResponse();

            for (SubjectiveResponse subjectiveResponse:sectionTwoResponses){
                sectionTwoContent.append(subjectiveResponse.getTopic());
                sectionTwoContent.append(",");
                sectionTwoContent.append(subjectiveResponse.getDuration());
                sectionTwoContent.append(",");
                sectionTwoContent.append(subjectiveResponse.getComment());
                sectionTwoContent.append('\n');
            }
        }
        return sectionTwoContent;
    }

    @RequestMapping(value = "/{cycleName}/appraisalSectionThree", method = RequestMethod.GET)
    public StringBuffer getAppraisalSectionThree (@PathVariable("cycleName") String cycleName) {

        AppraisalCycle appraisalCycle = appraisalCycleRepository.findByName(cycleName);
        List<Appraisal> appraisals = repository.findAllByCycleId(appraisalCycle.getId());
        StringBuffer sectionThreeContent = new StringBuffer();

        for (Appraisal appraisal:appraisals) {
            List<SubjectiveResponse> sectionThreeResponses = appraisal.getSectionthreeResponse();
            for (SubjectiveResponse subjectiveResponse:sectionThreeResponses){
                sectionThreeContent.append(subjectiveResponse.getTopic());
                sectionThreeContent.append(",");
                sectionThreeContent.append(subjectiveResponse.getDuration());
                sectionThreeContent.append(",");
                sectionThreeContent.append(subjectiveResponse.getComment());
                sectionThreeContent.append('\n');
            }
        }
        return sectionThreeContent;
    }

    @RequestMapping(value = "/{cycleName}/appraisalSectionFour", method = RequestMethod.GET)
    public StringBuffer getAppraisalSectionFour (@PathVariable("cycleName") String cycleName) {
        AppraisalCycle appraisalCycle = appraisalCycleRepository.findByName(cycleName);
        List<Appraisal> appraisals = repository.findAllByCycleId(appraisalCycle.getId());
        StringBuffer sectionFourContent = new StringBuffer();

        for (Appraisal appraisal:appraisals) {
            String sectionFourResponse = appraisal.getSectionfourResponse();
            sectionFourContent.append(sectionFourResponse);
            sectionFourContent.append('\n');
        }
        return sectionFourContent;
    }

    @RequestMapping(value = "/{cycleName}/appraisalSectionFive", method = RequestMethod.GET)
    public StringBuffer getAppraisalSectionFive (@PathVariable("cycleName") String cycleName) {
        AppraisalCycle appraisalCycle = appraisalCycleRepository.findByName(cycleName);
        List<Appraisal> appraisals = repository.findAllByCycleId(appraisalCycle.getId());
        StringBuffer sectionFiveContent = new StringBuffer();

        for (Appraisal appraisal:appraisals) {
            String sectionFiveResponse = appraisal.getSectionfiveResponse();
            sectionFiveContent.append(sectionFiveResponse);
            sectionFiveContent.append('\n');
        }
        return sectionFiveContent;
    }

    @RequestMapping(value = "/generate/report", method = RequestMethod.GET)
    public List<CsvObject> test(@RequestParam String cycleId){
        return service.generateReport(cycleId);
    }

    @GetMapping("/download/report")
    public void downloadCSV(HttpServletResponse response, @RequestParam String cycleId) throws IOException {

        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=Appraisal.csv");

        service.writeDataToCsvUsingStringArray(response.getWriter(), service.generateReport(cycleId));
    }

    @RequestMapping(value = "/cycle/{id}/user/{userId}/sectionone/custom", method = RequestMethod.PUT)
    public void saveCustomSectionOne (@PathVariable("id") String id, @PathVariable("userId") String userId,
                                @RequestBody List<ObjectiveResponseGroup> sectionone) {
        Appraisal appraisal = repository.findOneByCycleIdAndUserId(id, userId);
        appraisal.setSectiononeResponse(sectionone);
        repository.save(appraisal);
    }

    @RequestMapping(value = "/update/role", method = RequestMethod.POST)
    public void updateRoleInAppraisal (@RequestParam("file") MultipartFile file) throws IOException {
        Reader reader = new InputStreamReader(file.getInputStream());
        CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();

        for (String[] columns : csvReader.readAll()) {

            String cycleName =  columns[0].trim();             //CycleName
            String manager_person = columns[1].toLowerCase().trim();              //ManagerEmail
            String position = columns[2].trim();            //Relation
            String employee_person =  columns[3].toLowerCase().trim();             //User Email
            String action = columns[4].trim();            //Add & Remove

            AppraisalCycle cycle = appraisalCycleRepository.findByName(cycleName);

            if(cycle != null) {
                Person user = personRepository.findPersonByEmail(employee_person);

                if(user == null) {
                    return;
                }
                Person personUser = personRepository.findPersonByEmail(manager_person);

                if (personUser == null) {
                    return;
                }
                List<RoleType> roleTypes = new ArrayList<>();

                personUser.getRoles().stream().forEach(role -> {
                    roleTypes.add(role.getType());
                });

                if (!roleTypes.contains(RoleType.valueOf(position)) && action.equals("Add")) {
                    List<String> options = new ArrayList<>();
                    options.add(employee_person);

                    Role role = new Role();
                    role.setType(RoleType.valueOf(position));
                    role.setOptions(options);

                    List<Role> roles = personUser.getRoles();
                    roles.add(role);
                    personUser.setRoles(roles);

                }

                personUser.getRoles().stream().forEach(role -> {

                    if (role.getType().equals(RoleType.ProjectManager)) {
                        if (position.equals("ProjectManager") ) {
                            if (action.equals("Remove") && role.getOptions().contains(employee_person)) {
                                role.getOptions().remove(employee_person);
                            } else if (action.equals("Add") && !role.getOptions().contains(employee_person)) {
                                role.getOptions().add(employee_person);
                            }

                        }
                    }

                    if (role.getType().equals(RoleType.PracticeDirector)) {
                        if (position.equals("PracticeDirector")) {
                            if (action.equals("Remove") && role.getOptions().contains(employee_person)) {
                                role.getOptions().remove(employee_person);
                            } else if (action.equals("Add") && !role.getOptions().contains(employee_person)) {
                                role.getOptions().add(employee_person);
                            }

                        }
                    }

                    if (role.getType().equals(RoleType.TeamLead)) {
                        if (position.equals("TeamLead")) {
                            if (action.equals("Remove") && role.getOptions().contains(employee_person)) {
                                role.getOptions().remove(employee_person);
                            } else if (action.equals("Add") && !role.getOptions().contains(employee_person)) {
                                role.getOptions().add(employee_person);
                            }

                        }
                    }

                    if (role.getType().equals(RoleType.HR)) {
                        if (position.equals("HR")) {
                            if (action.equals("Remove") && role.getOptions().contains(employee_person)) {
                                role.getOptions().remove(employee_person);
                            } else if (action.equals("Add") && !role.getOptions().contains(employee_person)) {
                                role.getOptions().add(employee_person);
                            }

                        }
                    }

                });
                personRepository.save(personUser);


                Person manager = personRepository.findPersonByEmail(manager_person);
                Appraisal appraisal = repository.findOneByCycleIdAndUserId(cycle.getId(), user.getId());

                appraisal.getSectiononeResponse().stream().forEach(response-> {
                    response.getResponse().stream().forEach(res->{
                        if (position.equals("ProjectManager")) {
                            if (action.equals("Remove") && res.getProjectManagerReviews().containsKey(manager.getId())) {
                                res.getProjectManagerReviews().remove(manager.getId());
                            } else if (action.equals("Add") && !res.getProjectManagerReviews().containsKey(manager.getId())) {
                                res.getProjectManagerReviews().put(manager.getId(), getReviewElements(manager.getName()));
                            }
                        }

                        if (position.equals("PracticeDirector")) {
                            if (action.equals("Remove") && res.getPracticeDirectorReviews().containsKey(manager.getId())) {
                                res.getPracticeDirectorReviews().remove(manager.getId());
                            } else if (action.equals("Add") && !res.getPracticeDirectorReviews().containsKey(manager.getId())) {
                                res.getPracticeDirectorReviews().put(manager.getId(), getReviewElements(manager.getName()));
                            }
                        }

                        if (position.equals("TeamLead")) {
                            if (action.equals("Remove") && res.getTeamLeadReviews().containsKey(manager.getId())) {
                                res.getTeamLeadReviews().remove(manager.getId());
                            } else if (action.equals("Add") && !res.getTeamLeadReviews().containsKey(manager.getId())) {
                                res.getTeamLeadReviews().put(manager.getId(), getReviewElements(manager.getName()));
                            }
                        }

                        if (position.equals("HR")) {
                            if (action.equals("Remove") && res.getHrReviews().containsKey(manager.getId())) {
                                res.getHrReviews().remove(manager.getId());
                            } else if (action.equals("Add") && !res.getHrReviews().containsKey(manager.getId())) {
                                res.getHrReviews().put(manager.getId(), getReviewElements(manager.getName()));
                            }
                        }
                    });
                });

                repository.save(appraisal);
            }
        }
    }

    private ReviewerElements getReviewElements(String managerName) {
        return ReviewerElements
                .builder()
                .comment("")
                .name(managerName)
                .rating("")
                .isComplete(false)
                .build();
    }
}

@Data
class ErrorResource {
    List<String> sectionOneError = new ArrayList<>();
}

@Data
class StatusResource {
    String selfReview;
    String headReview;
    String scheduled;
    String complete;
}


