package com.jobsearch.service;
import com.jobsearch.model.Job;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {
    @Mock private JavaMailSender mailSender;
    @InjectMocks private EmailNotificationService emailNotificationService;

    @Test
    void sendJobAlert_WithJob_DoesNotThrow() {
        Job job = new Job();
        job.setTitle("Java Developer");
        job.setCompany("Google");
        job.setLocation("Remote");
        job.setJobType("Full-time");
        job.setSalary("$150,000");
        job.setDescription("Java Spring Boot microservices");
        job.setSourceUrl("https://google.com/jobs");
        assertDoesNotThrow(() -> emailNotificationService.sendJobAlert(job));
    }

    @Test
    void sendJobAlert_WithNullDescription_DoesNotThrow() {
        Job job = new Job();
        job.setTitle("Software Engineer");
        job.setCompany("Amazon");
        job.setLocation("Seattle");
        job.setJobType("Full-time");
        job.setDescription(null);
        job.setSourceUrl("https://amazon.com/jobs");
        assertDoesNotThrow(() -> emailNotificationService.sendJobAlert(job));
    }

    @Test
    void sendJobAlert_WithNullSalary_DoesNotThrow() {
        Job job = new Job();
        job.setTitle("Backend Engineer");
        job.setCompany("Meta");
        job.setLocation("NYC");
        job.setJobType("Full-time");
        job.setSalary(null);
        job.setDescription("Backend systems");
        job.setSourceUrl("https://meta.com/jobs");
        assertDoesNotThrow(() -> emailNotificationService.sendJobAlert(job));
    }
}
