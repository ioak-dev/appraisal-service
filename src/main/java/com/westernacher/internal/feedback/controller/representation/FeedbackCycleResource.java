package com.westernacher.internal.feedback.controller.representation;

import lombok.Data;

import java.util.List;

public class FeedbackCycleResource {
    @Data
    public static class QuestionResource {

        private List<String> questions;

    }
}
