package com.jobsearch.controller;

import com.jobsearch.model.Job;
import com.jobsearch.service.JobScraperService;
import com.jobsearch.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "Job search and filter endpoints")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "http://localhost:3000")
public class JobController {

    private final JobService jobService;
    private final JobScraperService jobScraperService;

    @GetMapping
    @Operation(summary = "Get all jobs with pagination")
    public ResponseEntity<Page<Job>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(jobService.getAllJobs(page, size));
    }

    @GetMapping("/search")
    @Operation(summary = "Search jobs by keyword")
    public ResponseEntity<Page<Job>> searchJobs(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(jobService.searchJobs(keyword, page, size));
    }

    @GetMapping("/filter/type")
    @Operation(summary = "Filter jobs by type")
    public ResponseEntity<Page<Job>> getByType(
            @RequestParam String jobType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(jobService.getJobsByType(jobType, page, size));
    }

    @GetMapping("/filter/experience")
    @Operation(summary = "Filter jobs by experience level")
    public ResponseEntity<Page<Job>> getByExperience(
            @RequestParam String level,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(jobService.getJobsByExperience(level, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job by ID")
    public ResponseEntity<Job> getJobById(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    @PostMapping("/sample")
    @Operation(summary = "Load sample jobs for testing")
    public ResponseEntity<String> loadSampleJobs() {
        jobService.createSampleJobs();
        return ResponseEntity.ok("Sample jobs loaded successfully!");
    }

    @PostMapping("/scrape")
    @Operation(summary = "Manually trigger job scraping")
    public ResponseEntity<String> triggerScrape() {
        jobScraperService.scrapeByKeyword("Java Developer");
        jobScraperService.scrapeByKeyword("Spring Boot Developer");
        return ResponseEntity.ok("Scraping started! Check logs for results.");
    }
}
