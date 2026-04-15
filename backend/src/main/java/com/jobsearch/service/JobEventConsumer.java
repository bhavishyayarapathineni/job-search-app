package com.jobsearch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobsearch.model.Job;
import com.jobsearch.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobEventConsumer {

    private final JobRepository jobRepository;
    private final ObjectMapper objectMapper;
    private final EmailNotificationService emailNotificationService;

    @KafkaListener(topics = "new-jobs", groupId = "job-search-group")
    public void consumeNewJob(String jobJson) {
        try {
            Job job = objectMapper.readValue(jobJson, Job.class);
            job.setId(null);
            Job savedJob = jobRepository.save(job);
            log.info("Saved job from Kafka: {}", job.getTitle());
            emailNotificationService.sendJobAlert(savedJob);
        } catch (Exception e) {
            log.error("Failed to consume job: {}", e.getMessage());
        }
    }
}
