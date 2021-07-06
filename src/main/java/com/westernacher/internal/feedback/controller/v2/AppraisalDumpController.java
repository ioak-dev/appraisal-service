package com.westernacher.internal.feedback.controller.v2;

import com.westernacher.internal.feedback.repository.AppraisalGoalRepository;
import com.westernacher.internal.feedback.repository.v2.AppraisalLongRepository;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("api/appraisal/databasedump")
public class AppraisalDumpController {

    @Autowired
    private AppraisalGoalRepository appraisalGoalRepository;

    @Autowired
    private AppraisalLongRepository appraisalLongRepository;

    @ApiOperation(value = "Provide dump of database collections", response = List.class)
    @GetMapping("/{collectionName}")
    public List getCollectionData(@PathVariable String collectionName) {
        switch (collectionName) {
            case "appraisal.goal":
                return appraisalGoalRepository.findAll();
            case "appraisal.long":
                return appraisalLongRepository.findAll();
            default:
                return Collections.emptyList();
        }
    }
}
