package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.controller.representation.ServiceRequestRepresentation;
import com.westernacher.internal.feedback.domain.ServiceRequest;
import com.westernacher.internal.feedback.domain.ServiceRequestStatusType;
import com.westernacher.internal.feedback.repository.ServiceRequestRepository;
import com.westernacher.internal.feedback.service.ServiceRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/request")
public class ServiceRequestController {

    @Autowired
    private ServiceRequestRepository repository;

    @Autowired
    private ServiceRequestService service;

    @RequestMapping(method = RequestMethod.GET)
    public List<ServiceRequest> getAll () {
        return repository.findAll();
    }

    @RequestMapping(value = "/user/{userId}", method = RequestMethod.GET)
    public List<ServiceRequest> get (@PathVariable("userId") String userId) {
        return repository.findAllByUserId(userId);
    }

    @RequestMapping(method = RequestMethod.POST)
    public void create (@Valid @RequestBody ServiceRequestRepresentation.CreateResource resource) {
        service.create(resource.getUserId(), resource.getModule(), resource.getRequestType(), resource.getDescription());
    }

    @RequestMapping(value = "/{id}/needAdditionalDetails", method = RequestMethod.POST)
    public void needAdditionalDetails (@PathVariable("id") String id, @RequestBody ServiceRequestRepresentation.NeedAdditionalDetailsResource resource) {
        service.addLog(ServiceRequestStatusType.NEED_INFORMATION, id, resource.getUserId(), resource.getComment());
    }

    @RequestMapping(value = "/{id}/provideAdditionalDetails", method = RequestMethod.POST)
    public void provideAdditionalDetails (@PathVariable("id") String id, @RequestBody ServiceRequestRepresentation.ProvideAdditionalDetailsResource resource) {
        service.addLog(ServiceRequestStatusType.OPEN, id, resource.getUserId(), resource.getComment());
    }

    @RequestMapping(value = "/{id}/resolve", method = RequestMethod.POST)
    public void resolve (@PathVariable("id") String id, @RequestBody ServiceRequestRepresentation.ProvideAdditionalDetailsResource resource) {
        service.addLog(ServiceRequestStatusType.CLOSED, id, resource.getUserId(), resource.getComment());
    }

    @RequestMapping(value = "/{id}/reopen", method = RequestMethod.POST)
    public void resolve (@PathVariable("id") String id, @RequestBody ServiceRequestRepresentation.ReopenResource resource) {
        service.addLog(ServiceRequestStatusType.OPEN, id, resource.getUserId(), resource.getComment());
    }

}


