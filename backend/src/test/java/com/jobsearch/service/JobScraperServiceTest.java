package com.jobsearch.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobScraperServiceTest {

    @Mock private JobEventProducer jobEventProducer;

    @InjectMocks private JobScraperService jobScraperService;

    @Test
    void scrapeByKeyword_InvalidKeyword_HandlesGracefully() {
        jobScraperService.scrapeByKeyword("java developer");
        // Should not throw exception
    }
}
