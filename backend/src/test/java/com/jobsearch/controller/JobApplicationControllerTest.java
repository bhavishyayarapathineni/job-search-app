package com.jobsearch.controller;

import com.jobsearch.model.JobApplication;
import com.jobsearch.service.JobApplicationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobApplicationControllerTest {

    @Mock private JobApplicationService jobApplicationService;
    @Mock private Authentication authentication;
    @InjectMocks private JobApplicationController jobApplicationController;

    @Test
    void create_ReturnsApplication() {
        JobApplication app = new JobApplication();
        app.setJobTitle("Engineer");
        app.setCompany("Google");
        when(authentication.getName()).thenReturn("test@gmail.com");
        when(jobApplicationService.create(any(), eq("test@gmail.com"))).thenReturn(app);
        ResponseEntity<JobApplication> response = jobApplicationController.create(app, authentication);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void getAll_ReturnsList() {
        when(authentication.getName()).thenReturn("test@gmail.com");
        when(jobApplicationService.getAll("test@gmail.com")).thenReturn(List.of(new JobApplication()));
        ResponseEntity<List<JobApplication>> response = jobApplicationController.getAll(authentication);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void updateStatus_ReturnsUpdated() {
        JobApplication app = new JobApplication();
        app.setStatus(JobApplication.ApplicationStatus.INTERVIEW);
        when(authentication.getName()).thenReturn("test@gmail.com");
        when(jobApplicationService.updateStatus(1L, JobApplication.ApplicationStatus.INTERVIEW, "test@gmail.com")).thenReturn(app);
        ResponseEntity<JobApplication> response = jobApplicationController.updateStatus(1L, Map.of("status", "INTERVIEW"), authentication);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void delete_ReturnsOk() {
        when(authentication.getName()).thenReturn("test@gmail.com");
        doNothing().when(jobApplicationService).delete(1L, "test@gmail.com");
        ResponseEntity<Void> response = jobApplicationController.delete(1L, authentication);
        assertEquals(200, response.getStatusCode().value());
    }
}
