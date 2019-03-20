package com.westernacher.internal.feedback.repository;

import com.westernacher.internal.feedback.domain.Feedback;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FeedbackRepository extends MongoRepository<Feedback, String> {
    Long deleteFeedbacksByCycleIdAndReceiverId(String cycleId, String receiverId);
    List<Feedback> findFeedbacksByCycleId(String cycleId);
    List<Feedback> findFeedbacksByCycleIdAndReceiverId(String cycleId, String receiverId);
    List<Feedback> findFeedbacksByCycleIdAndProviderId(String cycleId, String providerId);
    Feedback findFeedbackByCycleIdAndProviderIdAndReceiverId(String cycleId, String providerId, String receiverId);
    List<Feedback> findFeedbacksByReceiverId(String receiverId);
}
