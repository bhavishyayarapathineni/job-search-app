package com.jobsearch.controller;

import com.jobsearch.service.CoverLetterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/cover-letter")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CoverLetterController {

    private final CoverLetterService coverLetterService;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generate(
            @RequestBody Map<String, String> request,
            Authentication auth) {
        String coverLetter = coverLetterService.generate(
            request.get("jobTitle"),
            request.get("company"),
            request.get("jobDescription"),
            auth.getName()
        );
        return ResponseEntity.ok(Map.of("coverLetter", coverLetter));
    }
}
