package com.westernacher.internal.feedback.controller.representation;

import com.westernacher.internal.feedback.domain.FeedbackStatusType;
import lombok.Data;

import java.util.List;

public class FeedbackResource {
    @Data
    public static class UpdateProvidersResource {

        private String receiverId;
        private List<String> providerIds;

    }

    @Data
    public static class ApproveSelectionResource {

        private String receiverId;

    }

    @Data
    public static class RejectSelectionResource {

        private String receiverId;

    }

    @Data
    public static class ApproveFeedbackResource {

        private String receiverId;
        private String providerId;

    }

    @Data
    public static class RejectFeedbackResource {

        private String receiverId;
        private String providerId;

    }

    @Data
    public static class SaveResponseResource {
        private String receiverId;
        private String providerId;
        private List<String> responses;
    }

    @Data
    public static class SubmitResponseResource {
        private String receiverId;
        private String providerId;
    }

    @Data
    public static class ApproveResponseResource {
        private String receiverId;
        private String providerId;
    }

    @Data
    public static class RejectResponseResource {
        private String receiverId;
        private String providerId;
    }
}
