package com.westernacher.internal.feedback.service.v2;

import com.westernacher.internal.feedback.domain.v2.Person;
import org.springframework.web.multipart.MultipartFile;

public interface PersonService {
    Person createAndUpdate (Person person);

    void uploadPersonFile(MultipartFile file);
}
