package com.jobsearch.service;
import com.jobsearch.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JobScraperServiceTest {
    @Mock private JobRepository jobRepository;
    @Mock private JobEventProducer jobEventProducer;
    @InjectMocks private JobScraperService jobScraperService;

    @Test
    void jobScraperService_NotNull() {
        assertNotNull(jobScraperService);
    }

    @Test
    void scrapeByKeyword_NullKeyword_HandlesGracefully() {
        assertDoesNotThrow(() -> jobScraperService.scrapeByKeyword(null));
    }
}
