
package com.westernacher.internal.feedback.controller.v2;


import com.westernacher.internal.feedback.domain.v2.AppraisalLong;
import com.westernacher.internal.feedback.repository.v2.AppraisalLongRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/appraisal/custom/appraisallong")
@Slf4j
public class AppraisalLongController {

    @Autowired
    private AppraisalLongRepository repository;

    @GetMapping
    public ResponseEntity<List<AppraisalLong>> getAll (@RequestParam(required = false) String headerId) {
        if (headerId != null) {
            return ResponseEntity.ok(repository.findAllByHeaderId(headerId));
        }
        return ResponseEntity.ok(repository.findAll());
    }


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
