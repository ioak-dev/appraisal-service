package com.westernacher.internal.feedback.repository;

import com.westernacher.internal.feedback.domain.Person;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PersonRepository extends MongoRepository<Person, String> {

    List<Person> findAllByUnit(String unit);
    Person findPersonByEmail(String email);

    List<Person> findAllByCu(String cu);
}
