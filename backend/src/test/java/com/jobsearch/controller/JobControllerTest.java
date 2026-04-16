package com.jobsearch.controller;

import com.jobsearch.model.Job;
import com.jobsearch.service.JobService;
import com.jobsearch.security.JwtAuthFilter;
import com.jobsearch.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobController.class)
class JobControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private JobService jobService;
    @MockBean private JwtAuthFilter jwtAuthFilter;
    @MockBean private UserDetailsServiceImpl userDetailsService;

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
    @WithMockUser
    void getAllJobs_Returns200() throws Exception {
        Page<Job> page = new PageImpl<>(Arrays.asList(testJob));
        when(jobService.getAllJobs(anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/jobs?page=0&size=12"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void searchJobs_Returns200() throws Exception {
        Page<Job> page = new PageImpl<>(Arrays.asList(testJob));
        when(jobService.searchJobs(anyString(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/jobs/search?keyword=java&page=0&size=12"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void filterByType_Returns200() throws Exception {
        Page<Job> page = new PageImpl<>(Arrays.asList(testJob));
        when(jobService.getJobsByType(anyString(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/jobs/filter/type?jobType=FULL_TIME&page=0&size=12"))
            .andExpect(status().isOk());
    }
}
