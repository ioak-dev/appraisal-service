package com.westernacher.internal.feedback.domain;

public enum FeedbackStatusType {
    /**
     * When the feedback cycle is opened and waiting for the feedback receiver to
     * select the feedback providers.
     */
    PENDING_SELECTION,

    /**
     * Pending for head-of to approve the selected feedback providers to start the process.
     */
    REVIEW_SELECTION,

    /**
     * Submitted for review and waiting for the feedback comments from feedback provider.
     */
    PENDING_RESPONSE,

    /**
     * Feedback comments provided. Waiting for approval from head-of.
     */
    REVIEW_RESPONSE,

    /**
     * Feedback process is complete.
     */
    COMPLETED,

    /**
     * Opted out from feedback process by receiver.
     */
    OPTOUT
}
