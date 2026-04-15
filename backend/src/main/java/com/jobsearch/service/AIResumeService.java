package com.jobsearch.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Slf4j
@Service
public class AIResumeService {

    @Value("${openrouter.api.key:}")
    private String apiKey;

    private static final String OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";

    public Map<String, Object> analyzeAndTailor(
            String resumeText, String jobDescription,
            String jobTitle, String company) {

        Map<String, Object> result = new HashMap<>();

        Map<String, Object> beforeAnalysis = deepAnalyze(resumeText, jobDescription);
        int beforeScore = (int) beforeAnalysis.get("score");
        List<String> missingKeywords = (List<String>) beforeAnalysis.get("missingKeywords");
        List<String> matchedKeywords = (List<String>) beforeAnalysis.get("matchedKeywords");

        String tailoredResume;
        String feedback;

        if (apiKey != null && !apiKey.isEmpty()) {
            log.info("Using OpenRouter AI to tailor resume for: {} at {}", jobTitle, company);
            Map<String, Object> aiResult = callOpenRouter(
                resumeText, jobDescription, jobTitle, company, missingKeywords);
            tailoredResume = (String) aiResult.getOrDefault("resume", resumeText);
            feedback = (String) aiResult.getOrDefault("feedback", "Resume tailored successfully.");
        } else {
            log.warn("No API key found");
            tailoredResume = resumeText;
            feedback = "No API key configured.";
        }

        Map<String, Object> afterAnalysis = deepAnalyze(tailoredResume, jobDescription);
        int afterScore = (int) afterAnalysis.get("score");

        result.put("tailoredResume", tailoredResume);
        result.put("beforeScore", beforeScore);
        result.put("afterScore", afterScore);
        result.put("improvement", afterScore - beforeScore);
        result.put("matchedKeywords", matchedKeywords);
        result.put("missingKeywords", missingKeywords);
        result.put("feedback", feedback);
        result.put("fitLevel", getFitLevel(beforeScore));
        result.put("afterFitLevel", getFitLevel(afterScore));
        return result;
    }

    private Map<String, Object> callOpenRouter(
            String resume, String jd, String jobTitle,
            String company, List<String> missingKeywords) {

        Map<String, Object> result = new HashMap<>();
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("HTTP-Referer", "http://localhost:3000");
            headers.set("X-Title", "Job Search App");

            String prompt = String.format("""
You are an expert ATS resume optimizer and professional resume writer.

TARGET JOB: %s at %s
MISSING KEYWORDS TO ADD: %s

JOB DESCRIPTION:
%s

CANDIDATE'S CURRENT RESUME:
%s

REWRITE THIS RESUME to be a PERFECT match for this job:
1. Rewrite the summary to target this specific role
2. Rewrite every bullet point using exact keywords from the JD
3. Add missing keywords naturally into existing experience
4. Put most relevant skills first
5. NEVER fabricate experience, companies, or dates
6. Keep all real data — same companies, dates, education
7. Return ONLY the complete rewritten resume, nothing else
""", jobTitle, company,
    String.join(", ", missingKeywords.subList(0, Math.min(10, missingKeywords.size()))),
    jd, resume);

            Map<String, Object> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", "You are an expert resume writer and ATS optimization specialist.");

            Map<String, Object> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);

            Map<String, Object> body = new HashMap<>();
            body.put("model", "mistralai/mistral-small-3.1-24b-instruct:free");
            body.put("max_tokens", 4000);
            body.put("messages", List.of(systemMsg, userMsg));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                OPENROUTER_URL, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map> choices = (List<Map>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map choice = (Map) choices.get(0);
                    Map message = (Map) choice.get("message");
                    String tailored = null;
                    try {
                        // Try content field first
                        Object contentObj = message.get("content");
                        if (contentObj != null && !contentObj.toString().trim().isEmpty()) {
                            tailored = contentObj.toString();
                        }
                        // Do NOT use reasoning field - it contains thinking, not the resume
                        // Log all message keys for debugging
                        log.info("Message keys: {}, content length: {}", message.keySet(), 
                            tailored != null ? tailored.length() : 0);
                    } catch(Exception ex) {
                        log.error("Parse error: {}", ex.getMessage());
                    }
                    log.info("AI response length: {}", tailored != null ? tailored.length() : "null");
                    if (tailored == null || tailored.trim().isEmpty()) {
                        result.put("resume", resume);
                        result.put("feedback", "AI returned empty. Please try again.");
                        return result;
                    }
                    result.put("resume", tailored.trim());
                    result.put("feedback", "Resume rewritten by AI to perfectly match "
                        + jobTitle + " at " + company + "!");
                    log.info("OpenRouter AI tailored resume successfully!");
                    return result;
                }
            }
        } catch (Exception e) {
            log.error("OpenRouter error: {}", e.getMessage());
            result.put("feedback", "AI error: " + e.getMessage());
        }
        result.put("resume", resume);
        result.put("feedback", "AI tailoring failed. Please try again.");
        return result;
    }

    private Map<String, Object> deepAnalyze(String resume, String jd) {
        Map<String, Object> result = new HashMap<>();
        if (resume == null || jd == null) {
            result.put("score", 0);
            result.put("matchedKeywords", new ArrayList<>());
            result.put("missingKeywords", new ArrayList<>());
            return result;
        }
        List<String> techKeywords = Arrays.asList(
            "java","spring boot","spring","microservices","react","angular",
            "typescript","javascript","python","aws","azure","gcp","docker",
            "kubernetes","kafka","redis","postgresql","mongodb","mysql",
            "rest api","graphql","ci/cd","jenkins","git","agile","scrum",
            "hibernate","jpa","junit","mockito","maven","gradle","node.js",
            "sql","nosql","elasticsearch","terraform","linux","oauth","jwt",
            "security","distributed","cloud","devops","api gateway","lambda",
            "ec2","s3","rds","dynamodb","spring security","spring cloud",
            "sonarqube","jira","bitbucket","prometheus","grafana","elk stack"
        );
        String resumeLower = resume.toLowerCase();
        String jdLower = jd.toLowerCase();
        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String keyword : techKeywords) {
            if (jdLower.contains(keyword)) {
                if (resumeLower.contains(keyword)) matched.add(keyword);
                else missing.add(keyword);
            }
        }
        String[] jdWords = jd.split("\\W+");
        for (String word : jdWords) {
            if (word.length() > 4 && Character.isUpperCase(word.charAt(0))) {
                String lower = word.toLowerCase();
                if (!matched.contains(lower) && !missing.contains(lower)) {
                    if (resumeLower.contains(lower)) matched.add(lower);
                    else missing.add(lower);
                }
            }
        }
        int total = matched.size() + missing.size();
        int score = total > 0 ? Math.min(100, (matched.size() * 100) / total) : 50;
        result.put("score", score);
        result.put("matchedKeywords", matched);
        result.put("missingKeywords", missing.subList(0, Math.min(15, missing.size())));
        return result;
    }

    private String getFitLevel(int score) {
        if (score >= 80) return "EXCELLENT FIT";
        if (score >= 60) return "GOOD FIT";
        if (score >= 40) return "PARTIAL FIT";
        return "NEEDS IMPROVEMENT";
    }
}
