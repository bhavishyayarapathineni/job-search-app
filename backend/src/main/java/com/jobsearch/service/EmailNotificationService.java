package com.jobsearch.service;

import com.jobsearch.model.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailNotificationService {

    @Value("${sendgrid.api.key:demo}")
    private String sendGridApiKey;

    @Value("${notification.email:test@gmail.com}")
    private String notificationEmail;

    public void sendJobAlert(Job job) {
        log.info("NEW JOB ALERT!");
        log.info("Title: {}", job.getTitle());
        log.info("Company: {}", job.getCompany());
        log.info("Location: {}", job.getLocation());
        log.info("Apply: {}", job.getSourceUrl());
    }
}
