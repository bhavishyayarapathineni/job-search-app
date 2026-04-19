package com.jobsearch.controller;

import com.jobsearch.service.AIResumeService;
import com.jobsearch.service.ProfileService;
import com.jobsearch.model.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "https://bhavishyayarapathineni.github.io"})
public class AIResumeController {

    private final AIResumeService aiResumeService;
    private final ProfileService profileService;

    @PostMapping("/tailor-resume")
    public ResponseEntity<Map<String, Object>> tailorResume(
            Authentication auth,
            @RequestBody Map<String, String> request) {

        String jobDescription = request.get("jobDescription");
        String jobTitle = request.get("jobTitle");
        String company = request.get("company");

        UserProfile profile = profileService.getProfile(auth.getName());
        String resumeText = profile.getResumeText();

        if (resumeText == null || resumeText.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Please upload your resume in your Profile → Resume tab first!");
            return ResponseEntity.badRequest().body(error);
        }

        Map<String, Object> result = aiResumeService.analyzeAndTailor(
            resumeText, jobDescription, jobTitle, company
        );

        return ResponseEntity.ok(result);
    }
}
