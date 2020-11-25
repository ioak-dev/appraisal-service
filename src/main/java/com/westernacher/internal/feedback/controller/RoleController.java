
package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.domain.Role;
import com.westernacher.internal.feedback.repository.RoleRepository;
import com.westernacher.internal.feedback.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/role")
@Slf4j
public class RoleController {

    @Autowired
    private RoleRepository repository;

    @Autowired
    private RoleService service;

    @GetMapping
    public ResponseEntity<List<Role>> getRoles (@RequestParam(required = false) String reviewerId) {
        if (reviewerId == null) {
            return ResponseEntity.ok(repository.findAll());
        } else {
            return ResponseEntity.ok(repository.findByReviewerId(reviewerId));
        }
    }

    @PutMapping
    public ResponseEntity<Role> createAndUpdate (@RequestBody Role role) {
        return ResponseEntity.ok(service.createAndUpdate(role));
    }

    @DeleteMapping
    public void deleteAll () {
        repository.deleteAll();
    }

    @DeleteMapping("/{id}")
    public void delete (@PathVariable("id") String id) {
        repository.deleteById(id);
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadCsvFile(@RequestParam("file") MultipartFile file) {
        service.uploadCsvFile(file);
        return ResponseEntity.ok(HttpStatus.ACCEPTED);
    }

    @PostMapping(value = "/upload/reset", consumes = "multipart/form-data")
    public ResponseEntity<?> resetAnduploadCsvFile(@RequestParam("file") MultipartFile file) {
        service.resetAndUploadCsvFile(file);
        return ResponseEntity.ok(HttpStatus.ACCEPTED);
    }
}
