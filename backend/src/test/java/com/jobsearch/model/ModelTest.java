package com.jobsearch.model;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    @Test
    void user_GettersSetters() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@gmail.com");
        user.setFullName("Test User");
        user.setPassword("password");
        user.setPhone("1234567890");
        // role is enum - skip
        assertEquals(1L, user.getId());
        assertEquals("test@gmail.com", user.getEmail());
        assertEquals("Test User", user.getFullName());
    }

    @Test
    void job_GettersSetters() {
        Job job = new Job();
        job.setId(1L);
        job.setTitle("Java Developer");
        job.setCompany("Google");
        job.setLocation("Sunnyvale, CA");
        job.setSalary("$150k");
        job.setJobType("FULL_TIME");
        job.setExperienceLevel("SENIOR");
        job.setSource("Adzuna");
        job.setActive(true);
        job.setSkills(Arrays.asList("Java", "Spring Boot"));
        assertEquals(1L, job.getId());
        assertEquals("Java Developer", job.getTitle());
        assertEquals("Google", job.getCompany());
        assertTrue(job.isActive());
        assertEquals(2, job.getSkills().size());
    }

    @Test
    void userProfile_GettersSetters() {
        UserProfile profile = new UserProfile();
        profile.setId(1L);
        profile.setHeadline("Sr. Java Developer");
        profile.setPhone("+1 660 541 1976");
        profile.setLocation("Sunnyvale, CA");
        profile.setSummary("Experienced developer");
        profile.setSkills("Java, Spring Boot, React");
        profile.setResumeText("My resume");
        profile.setResumeFileName("resume.pdf");
        profile.setYearsOfExperience(5);
        profile.setOpenToRemote(true);
        profile.setOpenToRelocation(false);
        profile.setExpectedSalary(150000);
        assertEquals(1L, profile.getId());
        assertEquals("Sr. Java Developer", profile.getHeadline());
        assertEquals("My resume", profile.getResumeText());
        assertTrue(profile.getOpenToRemote());
        assertFalse(profile.getOpenToRelocation());
    }

    @Test
    void savedJob_GettersSetters() {
        SavedJob savedJob = new SavedJob();
        User user = new User();
        user.setId(1L);
        Job job = new Job();
        job.setId(1L);
        savedJob.setId(1L);
        savedJob.setUser(user);
        savedJob.setJob(job);
        assertEquals(1L, savedJob.getId());
        assertEquals(1L, savedJob.getUser().getId());
        assertEquals(1L, savedJob.getJob().getId());
    }

    @Test
    void jobApplication_GettersSetters() {
        JobApplication application = new JobApplication();
        User user = new User();
        user.setId(1L);
        Job job = new Job();
        job.setId(2L);

        application.setId(10L);
        application.setUser(user);
        application.setJob(job);
        application.setStatus(JobApplication.Status.APPLIED);
        application.setNotes("Applied via referral");

        assertEquals(10L, application.getId());
        assertEquals(1L, application.getUser().getId());
        assertEquals(2L, application.getJob().getId());
        assertEquals(JobApplication.Status.APPLIED, application.getStatus());
        assertEquals("Applied via referral", application.getNotes());
    }
}
