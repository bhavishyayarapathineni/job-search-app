package com.jobsearch.controller;

import com.jobsearch.model.SavedJob;
import com.jobsearch.model.UserProfile;
import com.jobsearch.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock private ProfileService profileService;
    @Mock private Authentication authentication;
    @InjectMocks private ProfileController controller;

    @Test
    void getProfile_ReturnsProfile() {
        UserProfile profile = new UserProfile();
        profile.setHeadline("Backend Engineer");
        when(authentication.getName()).thenReturn("user@test.com");
        when(profileService.getProfile("user@test.com")).thenReturn(profile);

        ResponseEntity<UserProfile> response = controller.getProfile(authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(profile, response.getBody());
    }

    @Test
    void updateProfile_ReturnsUpdatedProfile() {
        UserProfile incoming = new UserProfile();
        incoming.setSummary("Updated");
        when(authentication.getName()).thenReturn("user@test.com");
        when(profileService.updateProfile("user@test.com", incoming)).thenReturn(incoming);

        ResponseEntity<UserProfile> response = controller.updateProfile(authentication, incoming);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(incoming, response.getBody());
    }

    @Test
    void saveAndUnsaveJob_WorkAsExpected() {
        SavedJob savedJob = new SavedJob();
        savedJob.setId(10L);
        when(authentication.getName()).thenReturn("user@test.com");
        when(profileService.saveJob("user@test.com", 100L)).thenReturn(savedJob);

        ResponseEntity<SavedJob> saveResponse = controller.saveJob(authentication, 100L);
        ResponseEntity<Void> unsaveResponse = controller.unsaveJob(authentication, 100L);

        assertEquals(200, saveResponse.getStatusCode().value());
        assertEquals(savedJob, saveResponse.getBody());
        assertEquals(200, unsaveResponse.getStatusCode().value());
        verify(profileService).unsaveJob("user@test.com", 100L);
    }

    @Test
    void getSavedJobs_ReturnsList() {
        SavedJob savedJob = new SavedJob();
        savedJob.setId(5L);
        when(authentication.getName()).thenReturn("user@test.com");
        when(profileService.getSavedJobs("user@test.com")).thenReturn(List.of(savedJob));

        ResponseEntity<List<SavedJob>> response = controller.getSavedJobs(authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }
}
