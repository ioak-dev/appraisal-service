
package com.westernacher.internal.feedback.controller.v2;


import com.westernacher.internal.feedback.controller.representation.AppraisalLongResource;
import com.westernacher.internal.feedback.domain.v2.AppraisalDescriptive;
import com.westernacher.internal.feedback.domain.v2.AppraisalHeader;
import com.westernacher.internal.feedback.domain.v2.AppraisalLong;
import com.westernacher.internal.feedback.repository.v2.AppraisalHeaderRepository;
import com.westernacher.internal.feedback.repository.v2.AppraisalLongRepository;
import com.westernacher.internal.feedback.service.v2.AppraisalHeaderService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appraisal/custom/appraisallong")
@Slf4j
public class AppraisalLongController {

    @Autowired
    private AppraisalLongRepository repository;

    @Autowired
    private AppraisalHeaderRepository appraisalHeaderRepository;

    @Autowired
    private AppraisalHeaderService appraisalHeaderService;

    @ApiOperation(value = "get AppraisalLongResource by headerId",response = List.class)
    @GetMapping
    public ResponseEntity<List<AppraisalLongResource>> getAll (@RequestParam(required = false) String headerId) {

        Map<String, AppraisalHeader> appraisalHeaderMap = new HashMap<>();
        appraisalHeaderRepository.findAll().stream().forEach(appraisalHeader -> {
            appraisalHeaderMap.put(appraisalHeader.getId(), appraisalHeader);
        });

        List<AppraisalLong> headers = new ArrayList<>();

        if (headerId != null) {
            headers = repository.findAllByHeaderId(headerId);
        } else {
            headers = repository.findAll();
        }

        List<AppraisalLongResource> resources = new ArrayList<>();

        headers.stream().forEach(header->{
            resources.add(AppraisalLongResource.getAppraisalLongResource(header, appraisalHeaderMap));
        });

        resources.stream()
                .sorted(Comparator.comparing(AppraisalLongResource::getReviewerType)
        .thenComparing(AppraisalLongResource::getReviewerId)
                .thenComparing(AppraisalLongResource::getCreatedDate))
                .collect(Collectors.toList());

        return ResponseEntity.ok(resources);
    }

    @ApiOperation(value = "get AppraisalLongResource by employeeId and from and to",response = List.class)
    @GetMapping(value = "/custom")
    public ResponseEntity<List<AppraisalLongResource>> getByEmployeeId (@RequestParam String employeeId,
                                                                        @RequestParam String from,
                                                                        @RequestParam String to) {

        Map<String, AppraisalHeader> appraisalHeaderMap = new HashMap<>();
        appraisalHeaderService.getHeaderByEmployeeId(employeeId, from, to).stream().forEach(appraisalHeader -> {
            appraisalHeaderMap.put(appraisalHeader.getId(), appraisalHeader);
        });

        List<AppraisalLong> appraisalLongList = repository.findAllByHeaderIdIn(appraisalHeaderMap.keySet().stream().collect(Collectors.toList()));

        List<AppraisalLongResource> resources = new ArrayList<>();

        appraisalLongList.stream().forEach(header->{
            resources.add(AppraisalLongResource.getAppraisalLongResource(header, appraisalHeaderMap));
        });

        resources.stream()
                .sorted(Comparator.comparing(AppraisalLongResource::getReviewerType)
                        .thenComparing(AppraisalLongResource::getReviewerId)
                        .thenComparing(AppraisalLongResource::getCreatedDate))
                .collect(Collectors.toList());

        return ResponseEntity.ok(resources);
    }


    @ApiOperation(value = "create AppraisalLong ",response = AppraisalLong.class)
    @PostMapping
    public ResponseEntity<List<AppraisalLong>> create (@RequestBody List<AppraisalLong> appraisalLongs,
                                                       @RequestParam String headerId) {

        List<AppraisalLong> appraisalLongList = new ArrayList<>();
        appraisalLongs.stream().forEach(appraisalLong -> {
            appraisalLong.setHeaderId(headerId);
            appraisalLongList.add(appraisalLong);
        });

        return ResponseEntity.ok(repository.saveAll(appraisalLongList));
    }


}
