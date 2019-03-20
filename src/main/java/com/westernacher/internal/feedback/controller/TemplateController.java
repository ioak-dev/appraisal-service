package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.domain.Template;
import com.westernacher.internal.feedback.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/template")
public class TemplateController {

    @Autowired
    private TemplateRepository repository;

    @RequestMapping(method = RequestMethod.GET)
    public List<Template> getAll () {
        return repository.findAll();
    }

    @RequestMapping(value = "/unit/{unit}", method = RequestMethod.GET)
    public Template get (@PathVariable("unit") String unit) {
        return repository.findFirstByUnit(unit);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public Template update (@Valid @RequestBody Template template) {
        return repository.save(template);
    }

    @RequestMapping(value = "/unit/{unit}", method = RequestMethod.DELETE)
    public void delete (@PathVariable("unit") String unit) {
        Template template = repository.findFirstByUnit(unit);
        repository.delete(template);
    }
}
