package com.jobsearch.service;

import com.jobsearch.model.Job;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class EmailNotificationServiceTest {

    @Test
    void sendJobAlert_DoesNotThrow() {
        EmailNotificationService service = new EmailNotificationService();
        Job job = new Job();
        job.setTitle("Java Engineer");
        job.setCompany("Acme");
        job.setLocation("Remote");
        job.setSourceUrl("https://example.com/jobs/1");

        assertDoesNotThrow(() -> service.sendJobAlert(job));
    }
}
