package com.westernacher.internal.feedback.repository;

import com.westernacher.internal.feedback.domain.Template;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface TemplateRepository extends MongoRepository<Template, String> {
    Template findFirstByUnit(String unit);
}
