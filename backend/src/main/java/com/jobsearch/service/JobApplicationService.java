package com.jobsearch.service;

import com.jobsearch.model.JobApplication;
import com.jobsearch.model.User;
import com.jobsearch.repository.JobApplicationRepository;
import com.jobsearch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final UserRepository userRepository;

    public JobApplication create(JobApplication app, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        app.setUser(user);
        app.setAppliedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDateTime.now());
        return jobApplicationRepository.save(app);
    }

    public List<JobApplication> getAll(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return jobApplicationRepository.findByUserIdOrderByUpdatedAtDesc(user.getId());
    }

    public JobApplication updateStatus(Long id, JobApplication.ApplicationStatus status, String email) {
        JobApplication app = jobApplicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found"));
        if (!app.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }
        app.setStatus(status);
        app.setUpdatedAt(LocalDateTime.now());
        return jobApplicationRepository.save(app);
    }

    public JobApplication update(Long id, JobApplication updated, String email) {
        JobApplication app = jobApplicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found"));
        if (!app.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }
        app.setJobTitle(updated.getJobTitle());
        app.setCompany(updated.getCompany());
        app.setLocation(updated.getLocation());
        app.setSalary(updated.getSalary());
        app.setJobUrl(updated.getJobUrl());
        app.setNotes(updated.getNotes());
        app.setUpdatedAt(LocalDateTime.now());
        return jobApplicationRepository.save(app);
    }

    public void delete(Long id, String email) {
        JobApplication app = jobApplicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found"));
        if (!app.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }
        jobApplicationRepository.delete(app);
    }
}
