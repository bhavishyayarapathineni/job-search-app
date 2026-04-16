package com.jobsearch.service;

import com.jobsearch.model.Job;
import com.jobsearch.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock private JobRepository jobRepository;
    @InjectMocks private JobService jobService;

    private Job testJob;

    @BeforeEach
    void setUp() {
        testJob = new Job();
        testJob.setId(1L);
        testJob.setTitle("Java Developer");
        testJob.setCompany("Google");
        testJob.setLocation("Sunnyvale, CA");
        testJob.setSalary("$150k-$180k");
        testJob.setJobType("FULL_TIME");
        testJob.setExperienceLevel("SENIOR");
        testJob.setSource("Adzuna");
        testJob.setActive(true);
    }

    @Test
    void getAllJobs_ReturnsPagedJobs() {
        Pageable pageable = PageRequest.of(0, 12);
        Page<Job> mockPage = new PageImpl<>(Arrays.asList(testJob));
        when(jobRepository.findByIsActiveTrue(any(Pageable.class))).thenReturn(mockPage);

        Page<Job> result = jobService.getAllJobs(0, 12);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Java Developer", result.getContent().get(0).getTitle());
        verify(jobRepository, times(1)).findByIsActiveTrue(any(Pageable.class));
    }

    @Test
    void searchJobs_ReturnsMatchingJobs() {
        Pageable pageable = PageRequest.of(0, 12);
        Page<Job> mockPage = new PageImpl<>(Arrays.asList(testJob));
        when(jobRepository.searchJobs(anyString(), any(Pageable.class))).thenReturn(mockPage);

        Page<Job> result = jobService.searchJobs("Java", 0, 12);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(jobRepository, times(1)).searchJobs(anyString(), any(Pageable.class));
    }

    @Test
    void getJobsByType_ReturnsFilteredJobs() {
        Pageable pageable = PageRequest.of(0, 12);
        Page<Job> mockPage = new PageImpl<>(Arrays.asList(testJob));
        when(jobRepository.findByJobTypeAndIsActiveTrue(anyString(), any(Pageable.class))).thenReturn(mockPage);

        Page<Job> result = jobService.getJobsByType("FULL_TIME", 0, 12);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("FULL_TIME", result.getContent().get(0).getJobType());
    }

    @Test
    void getAllJobs_EmptyResult_ReturnsEmptyPage() {
        Page<Job> emptyPage = new PageImpl<>(Arrays.asList());
        when(jobRepository.findByIsActiveTrue(any(Pageable.class))).thenReturn(emptyPage);

        Page<Job> result = jobService.getAllJobs(0, 12);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void getJobsByExperience_ReturnsFilteredJobs() {
        Page<Job> mockPage = new PageImpl<>(List.of(testJob));
        when(jobRepository.findByExperienceLevelAndIsActiveTrue(anyString(), any(Pageable.class)))
            .thenReturn(mockPage);

        Page<Job> result = jobService.getJobsByExperience("SENIOR", 0, 12);

        assertEquals(1, result.getTotalElements());
        verify(jobRepository).findByExperienceLevelAndIsActiveTrue(anyString(), any(Pageable.class));
    }

    @Test
    void getJobById_WhenFound_ReturnsJob() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));

        Job result = jobService.getJobById(1L);

        assertEquals("Java Developer", result.getTitle());
    }

    @Test
    void getJobById_WhenMissing_Throws() {
        when(jobRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> jobService.getJobById(999L));

        assertTrue(ex.getMessage().contains("Job not found"));
    }

    @Test
    void saveJob_PersistsJob() {
        when(jobRepository.save(testJob)).thenReturn(testJob);

        Job saved = jobService.saveJob(testJob);

        assertEquals(testJob, saved);
        verify(jobRepository).save(testJob);
    }

    @Test
    void createSampleJobs_WhenRepositoryEmpty_SavesSamples() {
        when(jobRepository.count()).thenReturn(0L);

        jobService.createSampleJobs();

        verify(jobRepository).saveAll(anyList());
    }

    @Test
    void createSampleJobs_WhenRepositoryNotEmpty_DoesNothing() {
        when(jobRepository.count()).thenReturn(5L);

        jobService.createSampleJobs();

        verify(jobRepository, never()).saveAll(anyList());
    }
}
