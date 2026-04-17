package com.jobsearch.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Slf4j
@Service
@SuppressWarnings("java:S3776")
public class AIResumeService {

    @Value("${openrouter.api.key:}")
    private String apiKey;

    private static final String OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String KEY_SCORE = "score";
    private static final String KEY_RESUME = "resume";
    private static final String KEY_FEEDBACK = "feedback";
    private static final String KEY_MATCHED = "matchedKeywords";
    private static final String KEY_MISSING = "missingKeywords";
    private static final String KEY_CONTENT = "content";
    private static final List<String> MODELS = Arrays.asList(
        "nvidia/nemotron-3-nano-30b-a3b:free",
        "google/gemma-4-31b-it:free",
        "minimax/minimax-m2.5:free",
        "arcee-ai/trinity-large-preview:free"
    );

    public Map<String, Object> analyzeAndTailor(
            String resumeText, String jobDescription,
            String jobTitle, String company) {

        Map<String, Object> result = new HashMap<>();

        Map<String, Object> beforeAnalysis = deepAnalyze(resumeText, jobDescription);
        int beforeScore = (int) beforeAnalysis.get(KEY_SCORE);
        List<String> missingKeywords = (List<String>) beforeAnalysis.get(KEY_MISSING);
        List<String> matchedKeywords = (List<String>) beforeAnalysis.get(KEY_MATCHED);

        String tailoredResume;
        String feedback;

        String sanitizedApiKey = sanitizeApiKey(apiKey);
        if (sanitizedApiKey != null) {
            log.info("Using OpenRouter AI to tailor resume for: {} at {}", jobTitle, company);
            Map<String, Object> aiResult = callOpenRouter(
                resumeText, jobDescription, jobTitle, company, missingKeywords, sanitizedApiKey);
            tailoredResume = (String) aiResult.getOrDefault(KEY_RESUME, resumeText);
            feedback = (String) aiResult.getOrDefault(KEY_FEEDBACK, "Resume tailored successfully.");
        } else {
            log.warn("No API key found");
            tailoredResume = resumeText;
            feedback = "OpenRouter API key is missing or invalid. Set OPENROUTER_API_KEY and restart backend.";
        }

        Map<String, Object> afterAnalysis = deepAnalyze(tailoredResume, jobDescription);
        int afterScore = (int) afterAnalysis.get(KEY_SCORE);

        result.put("tailoredResume", tailoredResume);
        result.put("beforeScore", beforeScore);
        result.put("afterScore", afterScore);
        result.put("improvement", afterScore - beforeScore);
        result.put(KEY_MATCHED, matchedKeywords);
        result.put(KEY_MISSING, missingKeywords);
        result.put(KEY_FEEDBACK, feedback);
        result.put("fitLevel", getFitLevel(beforeScore));
        result.put("afterFitLevel", getFitLevel(afterScore));
        return result;
    }

    private Map<String, Object> callOpenRouter(
            String resume, String jd, String jobTitle,
            String company, List<String> missingKeywords, String sanitizedApiKey) {

        Map<String, Object> result = new HashMap<>();
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + sanitizedApiKey);
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
            systemMsg.put(KEY_CONTENT, "You are an expert resume writer and ATS optimization specialist.");

            Map<String, Object> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put(KEY_CONTENT, prompt);

            Map<String, Object> body = new HashMap<>();
            body.put("max_tokens", 4000);
            body.put("messages", List.of(systemMsg, userMsg));
            // Try each model until one works
            for (String m : MODELS) {
                body.put("model", m);
                try {
                    HttpEntity<Map<String, Object>> testReq = new HttpEntity<>(body, headers);
                    ResponseEntity<Map> testResp = restTemplate.postForEntity(OPENROUTER_URL, testReq, Map.class);
                    if (testResp.getStatusCode() == HttpStatus.OK) {
                        // Process response
                        List<Map<String, Object>> choices2 = (List<Map<String, Object>>) (List<?>) testResp.getBody().get("choices");
                        if (choices2 != null && !choices2.isEmpty()) {
                            Map<String, Object> msg2 = (Map<String, Object>) choices2.get(0).get("message");
                            Object ct2 = msg2 != null ? msg2.get(KEY_CONTENT) : null;
                            if (ct2 != null && !ct2.toString().trim().isEmpty()) {
                                result.put(KEY_RESUME, ct2.toString().trim());
                                result.put(KEY_FEEDBACK, "Resume rewritten by AI using " + m + " to match " + jobTitle + " at " + company + "!");
                                log.info("AI tailored successfully with model: {}", m);
                                return result;
                            }
                        }
                    }
                } catch (Exception me) {
                    log.warn("Model {} failed: {}", m, me.getMessage());
                }
            }
            log.error("All models failed - returning original resume");
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : "Unknown error";
            log.error("OpenRouter error: {}", message);
            if (message.contains("401")) {
                result.put(KEY_FEEDBACK, "OpenRouter authentication failed (401). Check OPENROUTER_API_KEY and restart backend.");
            } else {
                result.put(KEY_FEEDBACK, "AI error: " + message);
            }
        }
        result.put(KEY_RESUME, resume);
        result.putIfAbsent(KEY_FEEDBACK, "AI tailoring failed. Please try again.");
        return result;
    }

    private String sanitizeApiKey(String rawKey) {
        if (rawKey == null) return null;
        String key = rawKey.trim();
        if (key.isEmpty()) return null;
        // Guard against placeholder values and accidentally pasted non-key tokens.
        if ("your_openrouter_key_here".equalsIgnoreCase(key) || !key.startsWith("sk-or-")) {
            return null;
        }
        return key;
    }

    private Map<String, Object> deepAnalyze(String resume, String jd) {
        Map<String, Object> result = new HashMap<>();
        if (resume == null || jd == null) {
            result.put(KEY_SCORE, 0);
            result.put(KEY_MATCHED, new ArrayList<>());
            result.put(KEY_MISSING, new ArrayList<>());
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
        result.put(KEY_SCORE, score);
        result.put(KEY_MATCHED, matched);
        result.put(KEY_MISSING, missing.subList(0, Math.min(15, missing.size())));
        return result;
    }

    private String getFitLevel(int score) {
        if (score >= 80) return "EXCELLENT FIT";
        if (score >= 60) return "GOOD FIT";
        if (score >= 40) return "PARTIAL FIT";
        return "NEEDS IMPROVEMENT";
    }
}
