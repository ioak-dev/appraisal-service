package com.westernacher.internal.feedback.service;

import com.westernacher.internal.feedback.domain.Person;
import com.westernacher.internal.feedback.domain.Role;
import com.westernacher.internal.feedback.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PersonService {

    @Autowired
    private PersonRepository repository;

    public void updateRoles(String id, List<Role> roleList) {
        Person person = repository.findById(id).orElse(null);;

        person.setRoles(roleList);
        repository.save(person);
    }

    public void removeRoles(String id) {
        Person person = repository.findById(id).orElse(null);;

        person.setRoles(new ArrayList<>());
        repository.save(person);
    }
}
