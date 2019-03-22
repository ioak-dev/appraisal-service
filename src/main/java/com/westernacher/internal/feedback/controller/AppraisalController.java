package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.domain.*;
import com.westernacher.internal.feedback.repository.AppraisalCycleRepository;
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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/appraisal")
@Slf4j
public class AppraisalController {

    @Autowired
    private AppraisalRepository repository;

    @Autowired
    private AppraisalCycleRepository appraisalCycleRepository;

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
        objectiveResponseContent.append(",");
        objectiveResponseContent.append(objectiveResponse.getReviewerComment());
        objectiveResponseContent.append(",");
        objectiveResponseContent.append(objectiveResponse.getReviewerRating());
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


