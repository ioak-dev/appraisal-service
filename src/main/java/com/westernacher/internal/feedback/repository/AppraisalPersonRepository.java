package com.westernacher.internal.feedback.repository;

import com.westernacher.internal.feedback.domain.AppraisalPerson;
import com.westernacher.internal.feedback.domain.Person;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AppraisalPersonRepository extends MongoRepository<AppraisalPerson, String> {

    /*List<AppraisalPerson> findAllByUnit(String unit);

    AppraisalPerson findPersonByEmail(String email);

    List<AppraisalPerson> findAllByCu(String cu);*/
}
