package com.westernacher.internal.feedback.service;

import com.westernacher.internal.feedback.repository.AppraisalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppraisalService {

    @Autowired
    private AppraisalRepository repository;
}
