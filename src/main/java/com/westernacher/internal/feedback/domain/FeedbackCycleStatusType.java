package com.westernacher.internal.feedback.domain;

public enum FeedbackCycleStatusType {
    /**
     * When a new feedback cycle is created.
     */
    OPEN,

    /**
     * When a feedback cycle is activated and running currently.
     */
    ACTIVE,

    /**
     * When a feedback cycle has passed it's end date.
     */
    CLOSED
}
