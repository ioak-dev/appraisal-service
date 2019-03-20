package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.domain.*;
import com.westernacher.internal.feedback.repository.AppraisalRepository;
import com.westernacher.internal.feedback.service.AppraisalService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/appraisal")
@Slf4j
public class AppraisalController {

    @Autowired
    private AppraisalRepository repository;

    @Autowired
    private AppraisalService service;

    @RequestMapping(value = "/cycle/{id}", method = RequestMethod.GET)
    public List<Appraisal> getAllByCycle (@PathVariable("id") String id) {
        return repository.findAllByCycleId(id);
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

    @RequestMapping(value = "/cycle/{id}/user/{userId}/sectiontwo", method = RequestMethod.GET)
    public List<SubjectiveResponse> getSectionTwoByCycleAndUser (@PathVariable("id") String id, @PathVariable("userId") String userId) {
        return repository.findOneByCycleIdAndUserId(id, userId).getSectiontwoResponse();
    }

    @RequestMapping(value = "/cycle/{id}/user/{userId}/sectiontwo", method = RequestMethod.PUT)
    public void saveSectionTwo (@PathVariable("id") String id, @PathVariable("userId") String userId,
                                @RequestBody List<SubjectiveResponse> sectiontwo) {
        Appraisal appraisal = repository.findOneByCycleIdAndUserId(id, userId);
        appraisal.setSectiontwoResponse(sectiontwo);
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
                    if (objectiveResponse.getSelfComment()==null) {
                        sectionOneError.add(objectiveResponseGroup.getGroup()+" > "+objectiveResponse.getCriteria()+" > Comment");
                    } else if (objectiveResponse.getSelfComment().length()<50) {
                        sectionOneError.add(objectiveResponseGroup.getGroup()+" > "+objectiveResponse.getCriteria()+" > Comment should be atleast 50 characters");
                    }
                    if (objectiveResponse.getSelfRating()==null) {
                        sectionOneError.add(objectiveResponseGroup.getGroup()+" > "+objectiveResponse.getCriteria()+" > Rating");
                    }
                });
                errorResource.setSectionOneError(sectionOneError);
            });
        }

        if (errorResource.getSectionOneError().size()>0) {
            return new ResponseEntity<ErrorResource>(errorResource, HttpStatus.NOT_ACCEPTABLE);
        }else {
            appraisal.setStatus(AppraisalStatusType.HEAD_REVIEW);
            repository.save(appraisal);
            return new ResponseEntity<ErrorResource>(errorResource, HttpStatus.OK);
        }
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
                    if (objectiveResponse.getReviewerRating()==null) {
                        sectionOneError.add(objectiveResponseGroup.getGroup()+" > "+objectiveResponse.getCriteria()+" > Rating");
                    }
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
                    if (objectiveResponse.getSelfComment()==null) {
                        sectionOneError.add(objectiveResponseGroup.getGroup()+" > "+objectiveResponse.getCriteria()+" > Comment");
                    } else if (objectiveResponse.getSelfComment().length()<50) {
                        sectionOneError.add(objectiveResponseGroup.getGroup()+" > "+objectiveResponse.getCriteria()+" > Comment should be atleast 50 characters");
                    }
                    if (objectiveResponse.getSelfRating()==null) {
                        sectionOneError.add(objectiveResponseGroup.getGroup()+" > "+objectiveResponse.getCriteria()+" > Rating");
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
}

@Data
class ErrorResource {
    List<String> sectionOneError = new ArrayList<>();
}


