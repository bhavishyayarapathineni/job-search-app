package com.jobsearch.service;

import com.jobsearch.model.JobApplication;
import com.jobsearch.dto.JobApplicationRequest;
import com.jobsearch.model.User;
import com.jobsearch.repository.JobApplicationRepository;
import com.jobsearch.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock private JobApplicationRepository jobApplicationRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private JobApplicationService jobApplicationService;

    private User user;
    private JobApplication app;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@gmail.com");

        app = new JobApplication();
        app.setId(1L);
        app.setJobTitle("Software Engineer");
        app.setCompany("Google");
        app.setStatus(JobApplication.ApplicationStatus.APPLIED);
        app.setUser(user);
    }

    @Test
    void create_Success() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));
        when(jobApplicationRepository.save(any())).thenReturn(app);
        JobApplicationRequest request = new JobApplicationRequest();
        request.setJobTitle("Software Engineer");
        request.setCompany("Google");
        JobApplication result = jobApplicationService.create(request, "test@gmail.com");
        assertNotNull(result);
        assertEquals("Software Engineer", result.getJobTitle());
        verify(jobApplicationRepository, times(1)).save(any());
    }

    @Test
    void create_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail("notfound@gmail.com")).thenReturn(Optional.empty());
        JobApplicationRequest req = new JobApplicationRequest();
        Exception exception = assertThrows(RuntimeException.class, () -> jobApplicationService.create(req, "notfound@gmail.com"));
        assertNotNull(exception);
    }

    @Test
    void getAll_ReturnsApplications() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));
        when(jobApplicationRepository.findByUserIdOrderByUpdatedAtDesc(1L)).thenReturn(List.of(app));
        List<JobApplication> result = jobApplicationService.getAll("test@gmail.com");
        assertEquals(1, result.size());
        assertEquals("Google", result.get(0).getCompany());
    }

    @Test
    void updateStatus_Success() {
        when(jobApplicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(jobApplicationRepository.save(any())).thenReturn(app);
        JobApplication result = jobApplicationService.updateStatus(1L, JobApplication.ApplicationStatus.INTERVIEW, "test@gmail.com");
        assertNotNull(result);
        verify(jobApplicationRepository, times(1)).save(any());
    }

    @Test
    void updateStatus_Unauthorized_ThrowsException() {
        when(jobApplicationRepository.findById(1L)).thenReturn(Optional.of(app));
        assertThrows(RuntimeException.class, () ->
            jobApplicationService.updateStatus(1L, JobApplication.ApplicationStatus.INTERVIEW, "other@gmail.com"));
    }

    @Test
    void delete_Success() {
        when(jobApplicationRepository.findById(1L)).thenReturn(Optional.of(app));
        jobApplicationService.delete(1L, "test@gmail.com");
        verify(jobApplicationRepository, times(1)).delete(app);
    }

    @Test
    void delete_Unauthorized_ThrowsException() {
        when(jobApplicationRepository.findById(1L)).thenReturn(Optional.of(app));
        assertThrows(RuntimeException.class, () ->
            jobApplicationService.delete(1L, "other@gmail.com"));
    }
    @Test
    void update_Success() {
        JobApplicationRequest request = new JobApplicationRequest();
        request.setJobTitle("Senior Engineer");
        request.setCompany("Google");
        request.setLocation("Remote");
        request.setSalary("$200,000");
        request.setJobUrl("https://google.com");
        request.setNotes("Updated notes");
        when(jobApplicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(jobApplicationRepository.save(any())).thenReturn(app);
        JobApplication result = jobApplicationService.update(1L, request, "test@gmail.com");
        assertNotNull(result);
        verify(jobApplicationRepository, times(1)).save(any());
    }

    @Test
    void update_Unauthorized_ThrowsException() {
        when(jobApplicationRepository.findById(1L)).thenReturn(Optional.of(app));
        JobApplicationRequest req1 = new JobApplicationRequest();
        assertThrows(RuntimeException.class, () -> jobApplicationService.update(1L, req1, "other@gmail.com"));
    }

    @Test
    void update_NotFound_ThrowsException() {
        when(jobApplicationRepository.findById(99L)).thenReturn(Optional.empty());
        JobApplicationRequest req2 = new JobApplicationRequest();
        assertThrows(RuntimeException.class, () -> jobApplicationService.update(99L, req2, "test@gmail.com"));
    }

    @Test
    void updateStatus_NotFound_ThrowsException() {
        when(jobApplicationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () ->
            jobApplicationService.updateStatus(99L, JobApplication.ApplicationStatus.INTERVIEW, "test@gmail.com"));
    }

    @Test
    void getAll_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail("notfound@gmail.com")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () ->
            jobApplicationService.getAll("notfound@gmail.com"));
    }

    @Test
    void delete_NotFound_ThrowsException() {
        when(jobApplicationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () ->
            jobApplicationService.delete(99L, "test@gmail.com"));
    }

}
