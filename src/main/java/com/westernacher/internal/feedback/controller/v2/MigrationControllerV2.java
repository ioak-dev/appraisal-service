package com.westernacher.internal.feedback.controller.v2;

import com.westernacher.internal.feedback.domain.MigrationOutput;
import com.westernacher.internal.feedback.domain.v2.MigrationOutputV2;
import com.westernacher.internal.feedback.service.v2.MigrationServiceV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/migrateV2")
public class MigrationControllerV2 {

    @Autowired
    private MigrationServiceV2 serviceV2;

    @PostMapping("/prerequisiteData")
    public void migratePrerequisiteData() {
        serviceV2.migratePrerequisiteData();
    }

    @GetMapping("/getAppraisalData/{cycleId}")
    @ResponseStatus(HttpStatus.OK)
    public MigrationOutputV2 getAppraisalData(@PathVariable String cycleId){
        return serviceV2.getAppraisalData(cycleId);
    }

    @PostMapping("/loadAppraisalData")
    @ResponseStatus(HttpStatus.OK)
    public void loadAppraisalData(@RequestBody MigrationOutputV2 appraisalData){
        serviceV2.loadAppraisalData(appraisalData);
    }

}
