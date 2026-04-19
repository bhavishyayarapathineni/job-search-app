package com.jobsearch.controller;

import com.jobsearch.dto.JobApplicationRequest;
import com.jobsearch.model.JobApplication;
import com.jobsearch.service.JobApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    @PostMapping
    public ResponseEntity<JobApplication> create(
            @RequestBody JobApplicationRequest request, Authentication auth) {
        return ResponseEntity.ok(jobApplicationService.create(request, auth.getName()));
    }

    @GetMapping
    public ResponseEntity<List<JobApplication>> getAll(Authentication auth) {
        return ResponseEntity.ok(jobApplicationService.getAll(auth.getName()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<JobApplication> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        JobApplication.ApplicationStatus status =
            JobApplication.ApplicationStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(jobApplicationService.updateStatus(id, status, auth.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobApplication> update(
            @PathVariable Long id,
            @RequestBody JobApplicationRequest request,
            Authentication auth) {
        return ResponseEntity.ok(jobApplicationService.update(id, request, auth.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        jobApplicationService.delete(id, auth.getName());
        return ResponseEntity.ok().build();
    }
}
