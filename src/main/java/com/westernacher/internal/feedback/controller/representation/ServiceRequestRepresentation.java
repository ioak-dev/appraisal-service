package com.westernacher.internal.feedback.controller.representation;

import lombok.Data;

public class ServiceRequestRepresentation {
    @Data
    public static class CreateResource {
        private String userId;
        private String module;
        private String requestType;
        private String description;
    }

    @Data
    public static class NeedAdditionalDetailsResource {
        private String userId;
        private String comment;
    }

    @Data
    public static class ProvideAdditionalDetailsResource {
        private String userId;
        private String comment;
    }

    @Data
    public static class ResolveResource {
        private String userId;
        private String comment;
    }

    @Data
    public static class ReopenResource {
        private String userId;
        private String comment;
    }
}
