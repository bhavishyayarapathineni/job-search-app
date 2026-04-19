package com.jobsearch.service;

import com.jobsearch.dto.JobApplicationRequest;
import com.jobsearch.model.JobApplication;
import com.jobsearch.model.User;
import com.jobsearch.repository.JobApplicationRepository;
import com.jobsearch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private static final String APPLICATION_NOT_FOUND = "Application not found";

    private final JobApplicationRepository jobApplicationRepository;
    private final UserRepository userRepository;

    public JobApplication create(JobApplicationRequest request, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new NoSuchElementException("User not found"));
        JobApplication app = new JobApplication();
        app.setJobTitle(request.getJobTitle());
        app.setCompany(request.getCompany());
        app.setLocation(request.getLocation());
        app.setSalary(request.getSalary());
        app.setJobUrl(request.getJobUrl());
        app.setNotes(request.getNotes());
        app.setStatus(request.getStatus() != null ? request.getStatus() : JobApplication.ApplicationStatus.APPLIED);
        app.setUser(user);
        app.setAppliedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDateTime.now());
        return jobApplicationRepository.save(app);
    }

    public List<JobApplication> getAll(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new NoSuchElementException("User not found"));
        return jobApplicationRepository.findByUserIdOrderByUpdatedAtDesc(user.getId());
    }

    public JobApplication updateStatus(Long id, JobApplication.ApplicationStatus status, String email) {
        JobApplication app = jobApplicationRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException(APPLICATION_NOT_FOUND));
        if (!app.getUser().getEmail().equals(email)) {
            throw new SecurityException("Unauthorized");
        }
        app.setStatus(status);
        app.setUpdatedAt(LocalDateTime.now());
        return jobApplicationRepository.save(app);
    }

    public JobApplication update(Long id, JobApplicationRequest request, String email) {
        JobApplication app = jobApplicationRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException(APPLICATION_NOT_FOUND));
        if (!app.getUser().getEmail().equals(email)) {
            throw new SecurityException("Unauthorized");
        }
        app.setJobTitle(request.getJobTitle());
        app.setCompany(request.getCompany());
        app.setLocation(request.getLocation());
        app.setSalary(request.getSalary());
        app.setJobUrl(request.getJobUrl());
        app.setNotes(request.getNotes());
        app.setUpdatedAt(LocalDateTime.now());
        return jobApplicationRepository.save(app);
    }

    public void delete(Long id, String email) {
        JobApplication app = jobApplicationRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException(APPLICATION_NOT_FOUND));
        if (!app.getUser().getEmail().equals(email)) {
            throw new SecurityException("Unauthorized");
        }
        jobApplicationRepository.delete(app);
    }
}
