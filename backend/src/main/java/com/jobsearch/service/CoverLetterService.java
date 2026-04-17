package com.jobsearch.service;

import com.jobsearch.repository.UserProfileRepository;
import com.jobsearch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoverLetterService {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;

    @Value("${openrouter.api.key:}")
    private String apiKey;

    public String generate(String jobTitle, String company, String jobDescription, String email) {
        String resume = userRepository.findByEmail(email)
            .flatMap(user -> profileRepository.findByUserId(user.getId()))
            .map(p -> p.getResumeText() != null ? p.getResumeText() : "")
            .orElse("");

        return generateTemplate(jobTitle, company);
    }

    private String generateTemplate(String jobTitle, String company) {
        return "Dear Hiring Manager,\n\n"
            + "I am writing to express my strong interest in the " + jobTitle + " position at " + company + ". "
            + "With over 2 years of experience building scalable Java microservices and full-stack applications, "
            + "I am confident in my ability to make a meaningful contribution to your team.\n\n"
            + "In my current role at McKinsey & Company, I have designed and developed high-performance backend systems "
            + "using Java 17 and Spring Boot, improving system throughput by 40% for a platform serving 500K+ users. "
            + "I have implemented real-time data streaming pipelines using Apache Kafka, deployed containerized services "
            + "on AWS using Docker and Kubernetes, and maintained 88% code coverage through JUnit and Mockito testing.\n\n"
            + "I am particularly excited about the opportunity at " + company + ". "
            + "My experience with microservices architecture, cloud infrastructure, and modern DevOps practices "
            + "aligns well with your requirements.\n\n"
            + "Thank you for considering my application.\n\n"
            + "Best regards,\nBhavishya Yarapathineni\n"
            + "bhavishya123yarapathineni@gmail.com | +1 (660) 541-1976";
    }
}
