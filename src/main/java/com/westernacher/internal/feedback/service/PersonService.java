package com.westernacher.internal.feedback.service;

import com.westernacher.internal.feedback.domain.Person;
import org.springframework.web.multipart.MultipartFile;

public interface PersonService {
    Person createAndUpdate (Person person);

    void uploadPersonFile(MultipartFile file);
}
