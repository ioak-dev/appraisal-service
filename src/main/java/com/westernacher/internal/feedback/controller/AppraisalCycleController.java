package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.domain.AppraisalCycle;
import com.westernacher.internal.feedback.domain.AppraisalStatusType;
import com.westernacher.internal.feedback.domain.Report;
import com.westernacher.internal.feedback.repository.AppraisalCycleRepository;
import com.westernacher.internal.feedback.service.AppraisalCycleService;
import com.westernacher.internal.feedback.service.Implementation.AppraisalCycleResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/cycle")
public class AppraisalCycleController {

    @Autowired
    private AppraisalCycleRepository repository;

    @Autowired
    private AppraisalCycleService service;

    @GetMapping
    public List<AppraisalCycle> getAll () {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public AppraisalCycle get (@PathVariable("id") String id) {
        return repository.findById(id).orElse(null);
    }

    @PostMapping
    public AppraisalCycle create (@Valid @RequestBody AppraisalCycle appraisalCycle, @RequestParam(required = false) String sourceCycleId) {
        AppraisalCycle cycle = service.create(appraisalCycle);

        if (sourceCycleId != null) {
            service.copyPreviousAppraisalGoals(sourceCycleId, cycle.getId());
        }

        return cycle;
    }

    @PostMapping("/movetonextlevel/{id}")
    public List<String> movetonextlevel (@PathVariable String id, @RequestParam(required = false) String currentLevel, @RequestParam(required=false) String employeeId) {
        return service.movetonextlevel(id, currentLevel, employeeId, false);
    }

    @PostMapping("/movetopreviouslevel/{id}")
    public List<String> movetopreviouslevel (@PathVariable String id, @RequestParam(required = false) String currentLevel, @RequestParam(required=false) String employeeId) {
        return service.movetonextlevel(id, currentLevel, employeeId, true);
    }

    @DeleteMapping("/{id}")
    public AppraisalCycleResource.CycleDeleteResource delete (@PathVariable String id) {
        return service.delete(id);
    }

    @PostMapping("/copygoals/{sourceCycleId}/{destinationCycleId}")
    public void copyPreviousAppraisalGoals(@PathVariable String sourceCycleId,
                                           @PathVariable String destinationCycleId) {
        service.copyPreviousAppraisalGoals(sourceCycleId, destinationCycleId);
    }

    @GetMapping(value = "/printPDF/{cycleID}")
    @ResponseStatus(HttpStatus.OK)
    public String printPdf (@PathVariable String cycleID) {
        return service.printPdf(cycleID);
    }


}


