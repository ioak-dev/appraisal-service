package com.westernacher.internal.feedback.repository;

import com.westernacher.internal.feedback.domain.Person;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PersonRepository extends MongoRepository<Person, String> {
    List<Person> findAllByUnit(String unit);
    Person findPersonByEmail(String email);
    Person findPersonById(String id);

    List<Person> findByEmailIn(List<String> emails);

    @Query("{'email': {'$in': ?0}}")
    List<Person> findPersonsByEmail(List<String> emails);
}
