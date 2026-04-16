package com.jobsearch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobsearch.model.Job;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JobScraperServiceTest {

    @Mock private JobEventProducer jobEventProducer;
    private JobScraperService jobScraperService;

    @BeforeEach
    void setUp() {
        jobScraperService = new JobScraperService(jobEventProducer, new ObjectMapper());
    }

    @Test
    void scrapeByKeyword_InvalidKeyword_HandlesGracefully() {
        jobScraperService.scrapeByKeyword("java developer");
        // Should not throw exception
    }

    @Test
    void parseAndPublish_ParsesAndPublishesJobs() throws Exception {
        String body = """
            {
              "results": [
                {
                  "title": "Senior Java Developer",
                  "company": {"display_name": "Acme"},
                  "location": {"display_name": "Remote"},
                  "description": "Java Spring Boot Microservices Kafka Docker Kubernetes AWS",
                  "redirect_url": "https://example.com/job",
                  "salary_min": 120000,
                  "salary_max": 160000
                }
              ]
            }
            """;

        Method method = JobScraperService.class.getDeclaredMethod("parseAndPublish", String.class, String.class);
        method.setAccessible(true);
        method.invoke(jobScraperService, body, "java developer");

        verify(jobEventProducer, times(1)).publishNewJob(any(Job.class));
    }

    @Test
    void extractSkills_DetectsKnownSkills() throws Exception {
        Method method = JobScraperService.class.getDeclaredMethod("extractSkills", String.class);
        method.setAccessible(true);
        List<String> skills = (List<String>) method.invoke(jobScraperService,
            "Java Spring Boot React Microservices Kafka Docker Kubernetes AWS PostgreSQL REST API");

        assertTrue(skills.contains("Java"));
        assertTrue(skills.contains("Spring Boot"));
        assertTrue(skills.contains("AWS"));
    }

    @Test
    void detectJobTypeAndLevel_ReturnExpectedValues() throws Exception {
        Method detectType = JobScraperService.class.getDeclaredMethod("detectJobType", String.class);
        Method detectLevel = JobScraperService.class.getDeclaredMethod("detectLevel", String.class);
        detectType.setAccessible(true);
        detectLevel.setAccessible(true);

        assertEquals("CONTRACT", detectType.invoke(jobScraperService, "Java Developer C2C Contract"));
        assertEquals("PART_TIME", detectType.invoke(jobScraperService, "Part time Java role"));
        assertEquals("FULL_TIME", detectType.invoke(jobScraperService, "W2 full time Java role"));
        assertEquals("SENIOR", detectLevel.invoke(jobScraperService, "Senior Java Developer"));
        assertEquals("ENTRY", detectLevel.invoke(jobScraperService, "Junior Java Developer"));
        assertEquals("MID", detectLevel.invoke(jobScraperService, "Java Developer"));
    }
}
