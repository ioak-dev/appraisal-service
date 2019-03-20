package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.domain.Feedback;
import com.westernacher.internal.feedback.controller.representation.FeedbackResource;
import com.westernacher.internal.feedback.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService service;

    @RequestMapping(value = "/cycle/{cycleId}/updateProviders", method = RequestMethod.PUT)
    public void updateProviders(@PathVariable("cycleId") String cycleId,
                                @Valid @RequestBody FeedbackResource.UpdateProvidersResource resource) {
        service.updateProviders(cycleId, resource.getReceiverId(), resource.getProviderIds());
    }

    @RequestMapping(value = "/cycle/{cycleId}/submitProviders/receiver/{receiverId}", method = RequestMethod.POST)
    public void submitProviders(@PathVariable("cycleId") String cycleId,
                                @PathVariable("receiverId") String receiverId) {
        service.submitProviders(cycleId, receiverId);
    }

    @RequestMapping(value = "/cycle/{cycleId}", method = RequestMethod.GET)
    public List<Feedback> getFeedbackList(@PathVariable("cycleId") String cycleId) {
        return service.getFeedbackList(cycleId);
    }

    @RequestMapping(value = "/cycle/{cycleId}/receiver/{receiverId}", method = RequestMethod.GET)
    public List<Feedback> getFeedbackListByReceiver(@PathVariable("cycleId") String cycleId, @PathVariable("receiverId") String receiverId) {
        return service.getFeedbackListByReceiver(cycleId, receiverId);
    }

    @RequestMapping(value = "/cycle/{cycleId}/provider/{providerId}", method = RequestMethod.GET)
    public List<Feedback> getFeedbackListByProvider(@PathVariable("cycleId") String cycleId, @PathVariable("providerId") String providerId) {
        return service.getFeedbackListByProvider(cycleId, providerId);
    }

    @RequestMapping(value = "/receiver/{receiverId}", method = RequestMethod.GET)
    public List<Feedback> getFeedbackListAllCyclesByReceiver(@PathVariable("receiverId") String receiverId) {
        return service.getFeedbackListAllCyclesByReceiver(receiverId);
    }

    @RequestMapping(value = "/cycle/{cycleId}/receiver/{receiverId}/optout", method = RequestMethod.POST)
    public void optOut(@PathVariable("cycleId") String cycleId, @PathVariable("receiverId") String receiverId) {
        service.optOut(cycleId, receiverId);
    }

    @RequestMapping(value = "/cycle/{cycleId}/approveSelection", method = RequestMethod.POST)
    public void approveSelection(@PathVariable("cycleId") String cycleId,
                                 @Valid @RequestBody FeedbackResource.ApproveSelectionResource resource) {
        service.approveSelection(cycleId, resource.getReceiverId());
    }

    @RequestMapping(value = "/cycle/{cycleId}/rejectSelection", method = RequestMethod.POST)
    public void rejectSelection(@PathVariable("cycleId") String cycleId,
                                @Valid @RequestBody FeedbackResource.RejectSelectionResource resource) {
        service.rejectSelection(cycleId, resource.getReceiverId());
    }

    @RequestMapping(value = "/cycle/{cycleId}/approveFeedback", method = RequestMethod.POST)
    public void approveFeedback(@PathVariable("cycleId") String cycleId,
                                @Valid @RequestBody FeedbackResource.ApproveFeedbackResource resource) {
        service.approveFeedback(cycleId, resource.getProviderId(), resource.getReceiverId());
    }

    @RequestMapping(value = "/cycle/{cycleId}/rejectFeedback", method = RequestMethod.POST)
    public void rejectFeedback(@PathVariable("cycleId") String cycleId,
                               @Valid @RequestBody FeedbackResource.RejectFeedbackResource resource) {
        service.rejectFeedback(cycleId, resource.getProviderId(), resource.getReceiverId());
    }

    @RequestMapping(value = "/cycle/{cycleId}/saveResponse", method = RequestMethod.PUT)
    public void saveFeedback(@PathVariable("cycleId") String cycleId,
                             @Valid @RequestBody FeedbackResource.SaveResponseResource resource) {
        service.saveFeedback(cycleId, resource.getProviderId(), resource.getReceiverId(), resource.getResponses());
    }

    @RequestMapping(value = "/cycle/{cycleId}/submitResponse", method = RequestMethod.POST)
    public void submitFeedback(@PathVariable("cycleId") String cycleId,
                             @Valid @RequestBody FeedbackResource.SubmitResponseResource resource) {
        service.submitFeedback(cycleId, resource.getProviderId(), resource.getReceiverId());
    }

}


