package com.jobsearch.controller;

import com.jobsearch.model.UserProfile;
import com.jobsearch.service.AIResumeService;
import com.jobsearch.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIResumeControllerTest {

    @Mock private AIResumeService aiResumeService;
    @Mock private ProfileService profileService;
    @Mock private Authentication authentication;

    @InjectMocks private AIResumeController controller;

    @Test
    void tailorResume_ReturnsBadRequestWhenResumeMissing() {
        Map<String, String> request = new HashMap<>();
        request.put("jobDescription", "Need Java and Spring");
        request.put("jobTitle", "Java Dev");
        request.put("company", "ACME");

        UserProfile profile = new UserProfile();
        profile.setResumeText("  ");
        when(authentication.getName()).thenReturn("user@test.com");
        when(profileService.getProfile("user@test.com")).thenReturn(profile);

        ResponseEntity<Map<String, Object>> response = controller.tailorResume(authentication, request);

        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().containsKey("error"));
    }

    @Test
    void tailorResume_ReturnsAiResultWhenResumeExists() {
        Map<String, String> request = new HashMap<>();
        request.put("jobDescription", "Need Java and Spring");
        request.put("jobTitle", "Java Dev");
        request.put("company", "ACME");

        UserProfile profile = new UserProfile();
        profile.setResumeText("Strong Java experience");
        Map<String, Object> aiResult = Map.of("beforeScore", 50, "afterScore", 70);

        when(authentication.getName()).thenReturn("user@test.com");
        when(profileService.getProfile("user@test.com")).thenReturn(profile);
        when(aiResumeService.analyzeAndTailor("Strong Java experience", "Need Java and Spring", "Java Dev", "ACME"))
            .thenReturn(aiResult);

        ResponseEntity<Map<String, Object>> response = controller.tailorResume(authentication, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(aiResult, response.getBody());
        verify(aiResumeService).analyzeAndTailor("Strong Java experience", "Need Java and Spring", "Java Dev", "ACME");
    }
}
