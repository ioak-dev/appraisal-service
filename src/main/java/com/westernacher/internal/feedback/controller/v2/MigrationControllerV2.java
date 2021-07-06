package com.westernacher.internal.feedback.controller.v2;

import com.westernacher.internal.feedback.domain.v2.GetAndLoadOutput;
import com.westernacher.internal.feedback.domain.v2.MigrationOutputV2;
import com.westernacher.internal.feedback.service.v2.MigrationServiceV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/migrateV2")
public class MigrationControllerV2 {

    @Autowired
    private MigrationServiceV2 serviceV2;

    @GetMapping("/getAppraisalData/{cycleId}")
    @ResponseStatus(HttpStatus.OK)
    public MigrationOutputV2 getAppraisalData(@PathVariable String cycleId){
        return serviceV2.getAppraisalData(cycleId);
    }

    @PostMapping("/updateCuObjectives/{cycleId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateOrderForCUObjectives(@PathVariable String cycleId){
        serviceV2.updateCUObjectives(cycleId);
    }

    @PostMapping("/loadAppraisalData")
    @ResponseStatus(HttpStatus.OK)
    public void loadAppraisalData(@RequestBody MigrationOutputV2 appraisalData){
        serviceV2.loadAppraisalData(appraisalData);
    }

    @PostMapping("/getAndLoadAppraisalData/{cycleId}")
    @ResponseStatus(HttpStatus.OK)
    public GetAndLoadOutput getAndLoadAppraisalData(@PathVariable String cycleId){
        log.info("Updating CU Level Order ID's");
        serviceV2.updateCUObjectives(cycleId);
        log.info("getting appraisal data for cycleId " + cycleId);
        MigrationOutputV2 migrationOutputV2 = serviceV2.getAppraisalData(cycleId);
        log.info("Appraisal Data retrieved for cycleId " + cycleId);
        log.info("Loading retrieved appraisal data to DB");
        serviceV2.loadAppraisalData(migrationOutputV2);
        log.info("Migration Successful");
        return serviceV2.geMigrationOutputCount();
    }
}
