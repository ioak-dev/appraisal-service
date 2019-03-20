package com.westernacher.internal.feedback.service;

import com.westernacher.internal.feedback.domain.Feedback;
import com.westernacher.internal.feedback.domain.FeedbackStatusType;
import com.westernacher.internal.feedback.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository repository;

    public void updateProviders(String cycleId, String receiverId, List<String> providerIds) {
        repository.deleteFeedbacksByCycleIdAndReceiverId(cycleId, receiverId);
        providerIds.stream().forEach( providerId ->
            repository.save(Feedback.builder().cycleId(cycleId).receiverId(receiverId).providerId(providerId)
                    .status(FeedbackStatusType.PENDING_SELECTION).build())
        );
    }

    public void submitProviders(String cycleId, String receiverId) {
        repository.findFeedbacksByCycleIdAndReceiverId(cycleId, receiverId).forEach( feedback -> {
            feedback.setStatus(FeedbackStatusType.REVIEW_SELECTION);
            repository.save(feedback);
        });
    }

    public List<Feedback> getFeedbackList(String cycleId) {
        return repository.findFeedbacksByCycleId(cycleId);
    }

    public List<Feedback> getFeedbackListByReceiver(String cycleId, String receiverId) {
        return repository.findFeedbacksByCycleIdAndReceiverId(cycleId, receiverId);
    }

    public List<Feedback> getFeedbackListByProvider(String cycleId, String receiverId) {
        return repository.findFeedbacksByCycleIdAndProviderId(cycleId, receiverId);
    }

    public void optOut(String cycleId, String receiverId) {
        repository.deleteFeedbacksByCycleIdAndReceiverId(cycleId, receiverId);
        repository.save(Feedback.builder().cycleId(cycleId).receiverId(receiverId).status(FeedbackStatusType.OPTOUT).build());
    }

    public void approveSelection(String cycleId, String receiverId) {
        repository.findFeedbacksByCycleIdAndReceiverId(cycleId, receiverId).forEach( feedback -> {
            feedback.setStatus(FeedbackStatusType.PENDING_RESPONSE);
            repository.save(feedback);
        });
    }

    public void rejectSelection(String cycleId, String receiverId) {
        repository.findFeedbacksByCycleIdAndReceiverId(cycleId, receiverId).forEach( feedback -> {
            feedback.setStatus(FeedbackStatusType.PENDING_SELECTION);
            repository.save(feedback);
        });
    }

    public void approveFeedback(String cycleId, String providerId, String receiverId) {
        Feedback feedback = repository.findFeedbackByCycleIdAndProviderIdAndReceiverId(cycleId, providerId, receiverId);
        feedback.setStatus(FeedbackStatusType.COMPLETED);
        repository.save(feedback);
    }

    public void rejectFeedback(String cycleId, String providerId, String receiverId) {
        Feedback feedback = repository.findFeedbackByCycleIdAndProviderIdAndReceiverId(cycleId, providerId, receiverId);
        feedback.setStatus(FeedbackStatusType.PENDING_RESPONSE);
        repository.save(feedback);
    }

    public void saveFeedback(String cycleId, String providerId, String receiverId, List<String> responses) {
        Feedback feedback = repository.findFeedbackByCycleIdAndProviderIdAndReceiverId(cycleId, providerId, receiverId);
        feedback.setResponses(responses);
        repository.save(feedback);
    }

    public void submitFeedback(String cycleId, String receiverId, String providerId) {
        Feedback feedback = repository.findFeedbackByCycleIdAndProviderIdAndReceiverId(cycleId, receiverId, providerId);
        feedback.setStatus(FeedbackStatusType.REVIEW_RESPONSE);
        repository.save(feedback);
    }

    public List<Feedback> getFeedbackListAllCyclesByReceiver(String receiverId) {
        return repository.findFeedbacksByReceiverId(receiverId);
    }
}
