package com.jobsearch.service;

import com.jobsearch.model.Job;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("java:S3776")
public class JobScraperService {
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";

    private final JobEventProducer jobEventProducer;
    private final ObjectMapper objectMapper;

    @Value("${adzuna.app.id:50c354c7}")
    private String appId;

    @Value("${adzuna.app.key:9d892bf82bca5ec675feb1338130c172}")
    private String appKey;

    // All keywords covering full-time, contract, W2, C2C positions
    private static final List<String> KEYWORDS = List.of(
        "java developer",
        "java full stack developer",
        "spring boot developer",
        "java microservices developer",
        "senior java developer",
        "java contract w2",
        "java developer remote",
        "java developer google",
        "java developer amazon",
        "java developer microsoft",
        "java developer paypal",
        "java developer bank",
        "full stack react java developer",
        "java backend engineer",
        "java software engineer"
    );

    @Scheduled(fixedRate = 1800000)
    public void scrapeJobs() {
        log.info("Starting full job scrape...");
        for (String keyword : KEYWORDS) {
            scrapeByKeyword(keyword);
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); log.warn("Sleep interrupted"); }
        }
        log.info("Full scrape complete!");
    }

    public void scrapeByKeyword(String keyword) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String encodedKeyword = keyword.replace(" ", "+");
            String url = "https://api.adzuna.com/v1/api/jobs/us/search/1"
                    + "?app_id=" + appId
                    + "&app_key=" + appKey
                    + "&what=" + encodedKeyword
                    + "&results_per_page=50"
                    + "&sort_by=date";

            String response = restTemplate.getForObject(url, String.class);
            parseAndPublish(response, keyword);
        } catch (Exception e) {
            log.error("Scrape failed for {}: {}", keyword, e.getMessage());
        }
    }

    private void parseAndPublish(String body, String keyword) {
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode results = root.get("results");
            if (results == null || !results.isArray()) return;

            int count = 0;
            for (JsonNode node : results) {
                Job job = new Job();
                job.setTitle(getText(node, TITLE));
                job.setCompany(node.path("company")
                        .path("display_name").asText("Unknown"));
                job.setLocation(node.path("location")
                        .path("display_name").asText("USA"));
                job.setDescription(getText(node, DESCRIPTION));
                job.setJobType(detectJobType(getText(node, TITLE) + " " + getText(node, DESCRIPTION)));
                job.setSource("Adzuna");
                job.setSourceUrl(getText(node, "redirect_url"));
                job.setExperienceLevel(detectLevel(getText(node, TITLE)));
                job.setSalary(getSalary(node));
                job.setSkills(extractSkills(getText(node, DESCRIPTION)));
                jobEventProducer.publishNewJob(job);
                count++;
            }
            log.info("Published {} jobs for: {}", count, keyword);
        } catch (Exception e) {
            log.error("Parse failed: {}", e.getMessage());
        }
    }

    private String detectJobType(String text) {
        String lower = text.toLowerCase();
        if (lower.contains("contract") || lower.contains("c2c") || lower.contains("corp to corp")) return "CONTRACT";
        if (lower.contains("part time") || lower.contains("part-time")) return "PART_TIME";
        if (lower.contains("w2") || lower.contains("full time") || lower.contains("permanent")) return "FULL_TIME";
        return "FULL_TIME";
    }

    private String detectLevel(String title) {
        String lower = title.toLowerCase();
        if (lower.contains("senior") || lower.contains("sr.") || lower.contains("lead") || lower.contains("principal")) return "SENIOR";
        if (lower.contains("junior") || lower.contains("jr.") || lower.contains("entry")) return "ENTRY";
        return "MID";
    }

    private List<String> extractSkills(String description) {
        List<String> skills = new java.util.ArrayList<>();
        String lower = description.toLowerCase();
        if (lower.contains("java")) skills.add("Java");
        if (lower.contains("spring boot")) skills.add("Spring Boot");
        if (lower.contains("react")) skills.add("React");
        if (lower.contains("microservices")) skills.add("Microservices");
        if (lower.contains("kafka")) skills.add("Kafka");
        if (lower.contains("docker")) skills.add("Docker");
        if (lower.contains("kubernetes") || lower.contains("k8s")) skills.add("Kubernetes");
        if (lower.contains("aws")) skills.add("AWS");
        if (lower.contains("azure")) skills.add("Azure");
        if (lower.contains("python")) skills.add("Python");
        if (lower.contains("typescript")) skills.add("TypeScript");
        if (lower.contains("postgresql") || lower.contains("postgres")) skills.add("PostgreSQL");
        if (lower.contains("mongodb")) skills.add("MongoDB");
        if (lower.contains("rest api") || lower.contains("restful")) skills.add("REST APIs");
        if (skills.isEmpty()) skills.add("Java");
        return skills;
    }

    private String getSalary(JsonNode node) {
        double min = node.path("salary_min").asDouble(0);
        double max = node.path("salary_max").asDouble(0);
        if (min > 0 && max > 0)
            return String.format("$%.0fk-$%.0fk", min / 1000, max / 1000);
        return "Competitive";
    }

    private String getText(JsonNode node, String field) {
        JsonNode val = node.get(field);
        return (val != null && !val.isNull()) ? val.asText() : "";
    }
}
