package com.westernacher.internal.feedback.service;

import com.westernacher.internal.feedback.domain.AppraisalStatusType;
import com.westernacher.internal.feedback.domain.Person;
import com.westernacher.internal.feedback.repository.AppraisalRepository;
import com.westernacher.internal.feedback.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AppraisalService {

    @Autowired
    private AppraisalRepository repository;

    @Autowired
    private EmailUtility emailUtility;

    @Autowired
    private PersonRepository personRepository;

    @Async
    public void sendListOfMail(Set<String> idList, AppraisalStatusType position, String personId) {
        for (String userId:idList) {
            try {
                Person manager = personRepository.findById(userId).get();
                Person person = personRepository.findById(personId).get();
                String[] subjectParameters = new String[]{ person.getName() };
                String[] bodyParameters = new String[]{ person.getName(), manager.getName() };

                emailUtility.send(personRepository.findById(userId).get().getEmail(),
                        "subject_review", "body_review", subjectParameters, bodyParameters);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
