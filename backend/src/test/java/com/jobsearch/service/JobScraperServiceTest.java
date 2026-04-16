package com.jobsearch.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JobScraperServiceTest {

    @Mock private JobEventProducer jobEventProducer;
    @InjectMocks private JobScraperService jobScraperService;

    @Test
    void jobScraperService_NotNull() {
        assertNotNull(jobScraperService);
    }

    @Test
    void scrapeByKeyword_DoesNotThrow() {
        assertDoesNotThrow(() -> jobScraperService.scrapeByKeyword("java developer"));
    }

    @Test
    void scrapeByKeyword_NullKeyword_HandlesGracefully() {
        assertDoesNotThrow(() -> jobScraperService.scrapeByKeyword(null));
    }

    @Test
    void scrapeByKeyword_EmptyKeyword_HandlesGracefully() {
        assertDoesNotThrow(() -> jobScraperService.scrapeByKeyword(""));
    }
}
