package com.jobsearch.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JobEventConsumerTest {

    @Mock private com.jobsearch.repository.JobRepository jobRepository;
    @InjectMocks private JobEventConsumer jobEventConsumer;

    @Test
    void jobEventConsumer_NotNull() {
        assertNotNull(jobEventConsumer);
    }

    @Test
    void consumeJob_InvalidJson_HandlesGracefully() {
        assertDoesNotThrow(() -> jobEventConsumer.consumeNewJob("invalid json"));
    }
}
