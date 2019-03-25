package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.domain.Person;
import com.westernacher.internal.feedback.domain.Role;
import com.westernacher.internal.feedback.repository.PersonRepository;
import com.westernacher.internal.feedback.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/person")
public class PersonController {

    @Autowired
    private PersonRepository repository;

    @Autowired
    private PersonService service;

    @RequestMapping(method = RequestMethod.GET)
    public List<Person> getAll () {
        return repository.findAll();
    }

    @RequestMapping(value = "/unit/{unit}", method = RequestMethod.GET)
    public List<Person> getByUnit (@PathVariable("unit") String unit) {
        return repository.findAllByUnit(unit);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Person get (@PathVariable("id") String id) {
        return repository.findById(id).orElse(null);
    }

    @RequestMapping(value = "/email/{email}", method = RequestMethod.GET)
    public Person getPersonByEmail (@PathVariable("email") String email) {
        return repository.findPersonByEmail(email);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public Person update (@Valid @RequestBody Person person) {
        return repository.save(person);
    }

    @RequestMapping(method = RequestMethod.POST)
    public void create (@Valid @RequestBody List<Person> persons) {
        persons.forEach(person -> {
            person.setEmail(person.getEmail().toLowerCase());
            repository.save(person);
        });
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete (@PathVariable("id") String id) {
        repository.deleteById(id);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public void delete () {
        repository.deleteAll();
    }

    @RequestMapping(value = "/{id}/updateRoles", method = RequestMethod.PUT)
    public void updateRoles(@PathVariable("id") String id,
                                @Valid @RequestBody List<Role> roleList) {
        service.updateRoles(id, roleList);
    }

}
