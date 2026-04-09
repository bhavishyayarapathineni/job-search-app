package com.jobsearch.repository;

import com.jobsearch.model.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobRepository extends JpaRepository<Job, Long> {

    // Search by title or company or location
    @Query("SELECT j FROM Job j WHERE j.isActive = true AND " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.company) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.location) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Job> searchJobs(@Param("keyword") String keyword, Pageable pageable);

    // Filter by job type (FULL_TIME, CONTRACT etc)
    Page<Job> findByJobTypeAndIsActiveTrue(String jobType, Pageable pageable);

    // Filter by experience level
    Page<Job> findByExperienceLevelAndIsActiveTrue(
            String experienceLevel, Pageable pageable);

    // Get all active jobs
    Page<Job> findByIsActiveTrue(Pageable pageable);

    // Filter by source (LinkedIn, Indeed etc)
    Page<Job> findBySourceAndIsActiveTrue(String source, Pageable pageable);
}
