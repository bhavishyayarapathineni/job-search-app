package com.jobsearch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobsearch.model.Job;
import com.jobsearch.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobEventConsumerTest {

    @Mock private JobRepository jobRepository;
    @Mock private EmailNotificationService emailNotificationService;

    @Test
    void consumeNewJob_SavesAndNotifies() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jobJson = "{\"id\":99,\"title\":\"Senior Java Developer\",\"company\":\"Acme\"}";
        JobEventConsumer consumer = new JobEventConsumer(jobRepository, mapper, emailNotificationService);

        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));

        consumer.consumeNewJob(jobJson);

        verify(jobRepository).save(any(Job.class));
        verify(emailNotificationService).sendJobAlert(any(Job.class));
    }

    @Test
    void consumeNewJob_HandlesInvalidJson() {
        JobEventConsumer consumer = new JobEventConsumer(jobRepository, new ObjectMapper(), emailNotificationService);
        consumer.consumeNewJob("{invalid json");
    }
}
