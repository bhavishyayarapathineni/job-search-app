package com.jobsearch.controller;

import com.jobsearch.model.SavedJob;
import com.jobsearch.model.UserProfile;
import com.jobsearch.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "User profile and saved jobs")
@CrossOrigin(origins = "*")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    @Operation(summary = "Get user profile")
    public ResponseEntity<UserProfile> getProfile(Authentication auth) {
        return ResponseEntity.ok(profileService.getProfile(auth.getName()));
    }

    @PutMapping
    @Operation(summary = "Update user profile")
    public ResponseEntity<UserProfile> updateProfile(
            Authentication auth,
            @RequestBody UserProfile profile) {
        return ResponseEntity.ok(profileService.updateProfile(auth.getName(), profile));
    }

    @PostMapping("/jobs/{jobId}/save")
    @Operation(summary = "Save a job")
    public ResponseEntity<SavedJob> saveJob(
            Authentication auth,
            @PathVariable Long jobId) {
        return ResponseEntity.ok(profileService.saveJob(auth.getName(), jobId));
    }

    @DeleteMapping("/jobs/{jobId}/save")
    @Operation(summary = "Unsave a job")
    public ResponseEntity<Void> unsaveJob(
            Authentication auth,
            @PathVariable Long jobId) {
        profileService.unsaveJob(auth.getName(), jobId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/jobs/saved")
    @Operation(summary = "Get saved jobs")
    public ResponseEntity<List<SavedJob>> getSavedJobs(Authentication auth) {
        return ResponseEntity.ok(profileService.getSavedJobs(auth.getName()));
    }
}
