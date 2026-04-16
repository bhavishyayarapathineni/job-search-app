package com.jobsearch.controller;

import com.jobsearch.model.Job;
import com.jobsearch.service.JobScraperService;
import com.jobsearch.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobControllerTest {

    @Mock private JobService jobService;
    @Mock private JobScraperService jobScraperService;

    @InjectMocks private JobController jobController;

    private Job testJob;

    @BeforeEach
    void setUp() {
        testJob = new Job();
        testJob.setId(1L);
        testJob.setTitle("Java Developer");
        testJob.setCompany("Google");
        testJob.setLocation("Sunnyvale, CA");
        testJob.setSalary("$150k");
        testJob.setJobType("FULL_TIME");
        testJob.setExperienceLevel("SENIOR");
        testJob.setSource("Adzuna");
        testJob.setActive(true);
        testJob.setSkills(Arrays.asList("Java", "Spring Boot"));
    }

    @Test
    void getAllJobs_ReturnsOkWithPage() {
        Page<Job> page = new PageImpl<>(List.of(testJob));
        when(jobService.getAllJobs(0, 12)).thenReturn(page);

        ResponseEntity<Page<Job>> response = jobController.getAllJobs(0, 12);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(page, response.getBody());
    }

    @Test
    void searchJobs_ReturnsOkWithPage() {
        Page<Job> page = new PageImpl<>(List.of(testJob));
        when(jobService.searchJobs("java", 0, 12)).thenReturn(page);

        ResponseEntity<Page<Job>> response = jobController.searchJobs("java", 0, 12);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(page, response.getBody());
    }

    @Test
    void filterByType_ReturnsOkWithPage() {
        Page<Job> page = new PageImpl<>(List.of(testJob));
        when(jobService.getJobsByType("FULL_TIME", 0, 12)).thenReturn(page);

        ResponseEntity<Page<Job>> response = jobController.getByType("FULL_TIME", 0, 12);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(page, response.getBody());
    }

    @Test
    void getByExperience_ReturnsOkWithPage() {
        Page<Job> page = new PageImpl<>(List.of(testJob));
        when(jobService.getJobsByExperience("SENIOR", 0, 12)).thenReturn(page);

        ResponseEntity<Page<Job>> response = jobController.getByExperience("SENIOR", 0, 12);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(page, response.getBody());
    }

    @Test
    void getJobById_ReturnsOkWithJob() {
        when(jobService.getJobById(1L)).thenReturn(testJob);

        ResponseEntity<Job> response = jobController.getJobById(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(testJob, response.getBody());
    }

    @Test
    void loadSampleJobs_ReturnsSuccessMessage() {
        ResponseEntity<String> response = jobController.loadSampleJobs();

        verify(jobService).createSampleJobs();
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Sample jobs loaded successfully!", response.getBody());
    }

    @Test
    void triggerScrape_ReturnsSuccessMessage() {
        ResponseEntity<String> response = jobController.triggerScrape();

        verify(jobScraperService).scrapeByKeyword("Java Developer");
        verify(jobScraperService).scrapeByKeyword("Spring Boot Developer");
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Scraping started! Check logs for results.", response.getBody());
    }
}
