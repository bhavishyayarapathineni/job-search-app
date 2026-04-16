package com.jobsearch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jobsearch.model.Job;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JobEventProducerTest {

    @Mock private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void publishNewJob_SendsMessage() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        JobEventProducer producer = new JobEventProducer(kafkaTemplate, mapper);
        Job job = new Job();
        job.setTitle("Java Developer");
        job.setCompany("Acme");

        producer.publishNewJob(job);

        verify(kafkaTemplate).send(anyString(), anyString());
    }

    @Test
    void publishNewJob_HandlesSerializationError() {
        ObjectMapper brokenMapper = new ObjectMapper();
        JobEventProducer producer = new JobEventProducer(kafkaTemplate, brokenMapper);
        Job job = new Job();
        job.setTitle("Java Developer");
        // keep default LocalDateTime to force serialization error path
        assertDoesNotThrow(() -> producer.publishNewJob(job));
    }
}
