package com.jobsearch.service;

import com.jobsearch.model.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${notification.email:ybhavishya92@gmail.com}")
    private String notificationEmail;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public void sendJobAlert(Job job) {
        log.info("Job alert: {} at {}", job.getTitle(), job.getCompany());
        if (true) return; // Enable by configuring SMTP credentials
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(notificationEmail);
            helper.setSubject("New Job Alert: " + job.getTitle() + " at " + job.getCompany());
            helper.setText(buildEmailBody(job), true);
            mailSender.send(message);
            log.info("Email sent for job: {} at {}", job.getTitle(), job.getCompany());
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
        }
    }

    private String buildEmailBody(Job job) {
        String desc = job.getDescription() != null ?
            job.getDescription().substring(0, Math.min(300, job.getDescription().length())) + "..." : "";
        String salary = job.getSalary() != null ? job.getSalary() : "Not specified";
        return "<html><body style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto;'>"
            + "<div style='background:#1e3a5f;padding:20px;border-radius:10px 10px 0 0;'>"
            + "<h1 style='color:white;margin:0;'>New Job Alert!</h1></div>"
            + "<div style='padding:24px;border:1px solid #ddd;border-radius:0 0 10px 10px;'>"
            + "<h2 style='color:#1e3a5f;'>" + job.getTitle() + "</h2>"
            + "<p><strong>Company:</strong> " + job.getCompany() + "</p>"
            + "<p><strong>Location:</strong> " + job.getLocation() + "</p>"
            + "<p><strong>Job Type:</strong> " + job.getJobType() + "</p>"
            + "<p><strong>Salary:</strong> " + salary + "</p>"
            + "<p><strong>Description:</strong></p>"
            + "<p style='color:#555;'>" + desc + "</p>"
            + "<a href='" + job.getSourceUrl() + "' style='background:#1e3a5f;color:white;"
            + "padding:12px 24px;text-decoration:none;border-radius:6px;display:inline-block;margin-top:16px;'>"
            + "Apply Now</a></div></body></html>";
    }
}
