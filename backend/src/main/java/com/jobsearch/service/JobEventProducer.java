package com.jobsearch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobsearch.model.Job;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public static final String TOPIC = "new-jobs";

    // This sends a new job to Kafka topic
    // Think of it like sending a WhatsApp message
    public void publishNewJob(Job job) {
        try {
            String jobJson = objectMapper.writeValueAsString(job);
            kafkaTemplate.send(TOPIC, jobJson);
            log.info("Published job to Kafka: {}", job.getTitle());
        } catch (Exception e) {
            log.error("Failed to publish job: {}", e.getMessage());
        }
    }
}
