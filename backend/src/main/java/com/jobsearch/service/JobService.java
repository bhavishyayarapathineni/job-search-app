package com.jobsearch.service;

import com.jobsearch.model.Job;
import com.jobsearch.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    // Get all jobs with pagination
    public Page<Job> getAllJobs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("postedAt").descending());
        return jobRepository.findByIsActiveTrue(pageable);
    }

    // Search jobs by keyword
    public Page<Job> searchJobs(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("postedAt").descending());
        return jobRepository.searchJobs(keyword, pageable);
    }

    // Filter by job type
    public Page<Job> getJobsByType(String jobType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("postedAt").descending());
        return jobRepository.findByJobTypeAndIsActiveTrue(jobType, pageable);
    }

    // Filter by experience level
    public Page<Job> getJobsByExperience(String level, int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("postedAt").descending());
        return jobRepository.findByExperienceLevelAndIsActiveTrue(
                level, pageable);
    }

    // Get single job by ID
    public Job getJobById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Job not found with id: " + id));
    }

    // Save a new job (used by scraper)
    public Job saveJob(Job job) {
        log.info("Saving job: {} at {}", job.getTitle(), job.getCompany());
        return jobRepository.save(job);
    }

    // Add some sample jobs so we can test immediately
    public void createSampleJobs() {
        if (jobRepository.count() > 0) return;

        List<Job> sampleJobs = List.of(
            createJob("Senior Java Developer", "PayPal",
                    "San Jose, CA", "FULL_TIME", "SENIOR",
                    "Spring Boot, Microservices, Kafka, Docker",
                    "LinkedIn", "$150k-$180k"),
            createJob("Java Full Stack Engineer", "Bank of America",
                    "Charlotte, NC", "FULL_TIME", "SENIOR",
                    "Java, React, Azure, Spring Boot",
                    "Indeed", "$140k-$170k"),
            createJob("Backend Java Developer", "Capco",
                    "Charlotte, NC", "FULL_TIME", "MID",
                    "Java, Spring Boot, Microservices, REST APIs",
                    "LinkedIn", "$120k-$145k"),
            createJob("Full Stack Developer", "VLink",
                    "Burlington, MA", "FULL_TIME", "SENIOR",
                    "React, TypeScript, Node.js, AWS",
                    "Dice", "$130k-$160k"),
            createJob("Java Developer", "Freddie Mac",
                    "McLean, VA", "FULL_TIME", "MID",
                    "Java, Spring Boot, AWS, PostgreSQL",
                    "Indeed", "$115k-$140k")
        );

        jobRepository.saveAll(sampleJobs);
        log.info("Created {} sample jobs", sampleJobs.size());
    }

    private Job createJob(String title, String company, String location,
                          String jobType, String level, String skills,
                          String source, String salary) {
        Job job = new Job();
        job.setTitle(title);
        job.setCompany(company);
        job.setLocation(location);
        job.setJobType(jobType);
        job.setExperienceLevel(level);
        job.setDescription("We are looking for an experienced " +
                title + " to join our team at " + company);
        job.setSkills(List.of(skills.split(", ")));
        job.setSource(source);
        job.setSalary(salary);
        job.setSourceUrl("https://linkedin.com/jobs");
        return job;
    }
}
