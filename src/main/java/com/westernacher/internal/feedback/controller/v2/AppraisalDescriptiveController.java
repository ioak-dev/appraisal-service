
package com.westernacher.internal.feedback.controller.v2;


import com.westernacher.internal.feedback.controller.representation.AppraisalDescriptiveResource;
import com.westernacher.internal.feedback.controller.representation.AppraisalLongResource;
import com.westernacher.internal.feedback.domain.v2.AppraisalDescriptive;
import com.westernacher.internal.feedback.domain.v2.AppraisalHeader;
import com.westernacher.internal.feedback.domain.v2.AppraisalLong;
import com.westernacher.internal.feedback.repository.v2.AppraisalDescriptiveRepository;
import com.westernacher.internal.feedback.repository.v2.AppraisalHeaderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/descriptive")
@Slf4j
public class AppraisalDescriptiveController {

    @Autowired
    private AppraisalDescriptiveRepository repository;

    @Autowired
    private AppraisalHeaderRepository appraisalHeaderRepository;

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

        /*resources.stream()
                .sorted(Comparator.comparing(AppraisalDescriptiveResource::getReviewerType)
        .thenComparing(AppraisalDescriptiveResource::getReviewerId))
                .collect(Collectors.toList());*/

        return ResponseEntity.ok(resources);
    }


    @PostMapping
    public ResponseEntity<AppraisalDescriptive> create (@RequestBody AppraisalDescriptive appraisalDescriptives,
                                                       @RequestParam String headerId) {

        appraisalDescriptives.setHeaderId(headerId);

        return ResponseEntity.ok(repository.save(appraisalDescriptives));
    }


}
