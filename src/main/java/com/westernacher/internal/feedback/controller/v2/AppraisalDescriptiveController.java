
package com.westernacher.internal.feedback.controller.v2;


import com.westernacher.internal.feedback.controller.representation.AppraisalDescriptiveResource;
import com.westernacher.internal.feedback.controller.representation.AppraisalLongResource;
import com.westernacher.internal.feedback.domain.v2.AppraisalDescriptive;
import com.westernacher.internal.feedback.domain.v2.AppraisalHeader;
import com.westernacher.internal.feedback.domain.v2.AppraisalLong;
import com.westernacher.internal.feedback.repository.v2.AppraisalDescriptiveRepository;
import com.westernacher.internal.feedback.repository.v2.AppraisalHeaderRepository;
import com.westernacher.internal.feedback.service.v2.AppraisalHeaderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/descriptive")
@Slf4j
public class AppraisalDescriptiveController {

    @Autowired
    private AppraisalDescriptiveRepository repository;

    @Autowired
    private AppraisalHeaderRepository appraisalHeaderRepository;

    @Autowired
    private AppraisalHeaderService appraisalHeaderService;

    @GetMapping
    public ResponseEntity<List<AppraisalDescriptiveResource>> getAll (@RequestParam(required = false) String headerId) {

        Map<String, AppraisalHeader> appraisalHeaderMap = new HashMap<>();
        appraisalHeaderRepository.findAll().stream().forEach(appraisalHeader -> {
            appraisalHeaderMap.put(appraisalHeader.getId(), appraisalHeader);
        });

        List<AppraisalDescriptive> headers = new ArrayList<>();

        if (headerId != null) {
            headers = repository.findAllByHeaderId(headerId);
        } else {
            headers = repository.findAll();
        }

        List<AppraisalDescriptiveResource> resources = new ArrayList<>();

        headers.stream().forEach(header->{
            resources.add(AppraisalDescriptiveResource.getAppraisalDescriptiveResource(header, appraisalHeaderMap));
        });

        resources.stream()
                .sorted(Comparator.comparing(AppraisalDescriptiveResource::getReviewerType)
        .thenComparing(AppraisalDescriptiveResource::getReviewerId))
                .collect(Collectors.toList());

        return ResponseEntity.ok(resources);
    }

    @GetMapping(value = "/custom")
    public ResponseEntity<List<AppraisalDescriptiveResource>> getByEmployeeId (@RequestParam String employeeId,
                                                                        @RequestParam String from,
                                                                        @RequestParam String to) {

        Map<String, AppraisalHeader> appraisalHeaderMap = new HashMap<>();
        appraisalHeaderService.getHeaderByEmployeeId(employeeId, from, to).stream().forEach(appraisalHeader -> {
            appraisalHeaderMap.put(appraisalHeader.getId(), appraisalHeader);
        });

        List<AppraisalDescriptive> appraisalDescriptives = repository.findAllByHeaderIdIn(appraisalHeaderMap.keySet().stream().collect(Collectors.toList()));

        List<AppraisalDescriptiveResource> resources = new ArrayList<>();

        appraisalDescriptives.stream().forEach(header->{
            resources.add(AppraisalDescriptiveResource.getAppraisalDescriptiveResource(header, appraisalHeaderMap));
        });

        resources.stream()
                .sorted(Comparator.comparing(AppraisalDescriptiveResource::getReviewerType)
                        .thenComparing(AppraisalDescriptiveResource::getReviewerId)
                        .thenComparing(AppraisalDescriptiveResource::getCreatedDate))
                .collect(Collectors.toList());

        return ResponseEntity.ok(resources);
    }


    @PostMapping
    public ResponseEntity<AppraisalDescriptive> create (@RequestBody AppraisalDescriptive appraisalDescriptives,
                                                       @RequestParam String headerId) {

        appraisalDescriptives.setHeaderId(headerId);

        return ResponseEntity.ok(repository.save(appraisalDescriptives));
    }


}
