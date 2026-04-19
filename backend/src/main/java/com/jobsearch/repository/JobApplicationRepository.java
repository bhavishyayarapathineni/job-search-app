package com.jobsearch.repository;

import com.jobsearch.model.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByUserIdOrderByUpdatedAtDesc(Long userId);
    List<JobApplication> findByUserIdAndStatus(Long userId, JobApplication.ApplicationStatus status);
}
