package com.jobsearch.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AIResumeServiceTest {

    @InjectMocks private AIResumeService aiResumeService;

    private String sampleResume;
    private String sampleJD;

    @BeforeEach
    void setUp() {
        sampleResume = """
            John Doe - Java Developer
            Skills: Java, Spring Boot, React, AWS, Docker, Kubernetes
            Experience: 3 years at McKinsey building microservices
            Education: M.S. Computer Science
            """;

        sampleJD = """
            We are looking for a Senior Java Developer with experience in:
            Java, Spring Boot, Microservices, Kafka, Docker, Kubernetes, AWS
            Must have REST API development experience and CI/CD knowledge
            """;
    }

    @Test
    void analyzeAndTailor_NoApiKey_ReturnsOriginalResume() {
        Map<String, Object> result = aiResumeService.analyzeAndTailor(
            sampleResume, sampleJD, "Java Developer", "Google"
        );

        assertNotNull(result);
        assertNotNull(result.get("tailoredResume"));
        assertNotNull(result.get("beforeScore"));
        assertNotNull(result.get("afterScore"));
        assertNotNull(result.get("matchedKeywords"));
        assertNotNull(result.get("missingKeywords"));
        assertNotNull(result.get("fitLevel"));
    }

    @Test
    void analyzeAndTailor_CalculatesATSScore() {
        Map<String, Object> result = aiResumeService.analyzeAndTailor(
            sampleResume, sampleJD, "Java Developer", "Google"
        );

        int beforeScore = (int) result.get("beforeScore");
        assertTrue(beforeScore >= 0 && beforeScore <= 100);
    }

    @Test
    void analyzeAndTailor_FindsMatchedKeywords() {
        Map<String, Object> result = aiResumeService.analyzeAndTailor(
            sampleResume, sampleJD, "Java Developer", "Google"
        );

        List<String> matched = (List<String>) result.get("matchedKeywords");
        assertNotNull(matched);
        assertTrue(matched.size() > 0);
        assertTrue(matched.contains("java") || matched.contains("spring boot") ||
                   matched.contains("docker") || matched.contains("kubernetes"));
    }

    @Test
    void analyzeAndTailor_FindsMissingKeywords() {
        String limitedResume = "John Doe - Java Developer. Skills: Java only.";
        Map<String, Object> result = aiResumeService.analyzeAndTailor(
            limitedResume, sampleJD, "Java Developer", "Google"
        );

        List<String> missing = (List<String>) result.get("missingKeywords");
        assertNotNull(missing);
        assertTrue(missing.size() > 0);
    }

    @Test
    void analyzeAndTailor_NullResume_HandlesGracefully() {
        Map<String, Object> result = aiResumeService.analyzeAndTailor(
            null, sampleJD, "Java Developer", "Google"
        );

        assertNotNull(result);
        assertNotNull(result.get("feedback"));
    }

    @Test
    void analyzeAndTailor_FitLevelCorrect() {
        Map<String, Object> result = aiResumeService.analyzeAndTailor(
            sampleResume, sampleJD, "Java Developer", "Google"
        );

        String fitLevel = (String) result.get("fitLevel");
        assertNotNull(fitLevel);
        assertTrue(fitLevel.equals("EXCELLENT FIT") || fitLevel.equals("GOOD FIT") ||
                   fitLevel.equals("PARTIAL FIT") || fitLevel.equals("NEEDS IMPROVEMENT"));
    }

    @Test
    void analyzeAndTailor_WithApiKey_HandlesExternalCallFailureGracefully() throws Exception {
        Field apiKeyField = AIResumeService.class.getDeclaredField("apiKey");
        apiKeyField.setAccessible(true);
        apiKeyField.set(aiResumeService, "sk-or-test-key");

        Map<String, Object> result = aiResumeService.analyzeAndTailor(
            sampleResume, sampleJD, "Java Developer", "Google"
        );

        assertNotNull(result.get("tailoredResume"));
        assertNotNull(result.get("feedback"));
    }

    @Test
    void analyzeAndTailor_WithPlaceholderKey_TreatsAsMissingKey() throws Exception {
        Field apiKeyField = AIResumeService.class.getDeclaredField("apiKey");
        apiKeyField.setAccessible(true);
        apiKeyField.set(aiResumeService, "your_openrouter_key_here");

        Map<String, Object> result = aiResumeService.analyzeAndTailor(
            sampleResume, sampleJD, "Java Developer", "Google"
        );

        assertEquals("OpenRouter API key is missing or invalid. Set OPENROUTER_API_KEY and restart backend.",
            result.get("feedback"));
    }

    @Test
    void analyzeAndTailor_WithNonOpenRouterPrefix_TreatsAsMissingKey() throws Exception {
        Field apiKeyField = AIResumeService.class.getDeclaredField("apiKey");
        apiKeyField.setAccessible(true);
        apiKeyField.set(aiResumeService, "abc123");

        Map<String, Object> result = aiResumeService.analyzeAndTailor(
            sampleResume, sampleJD, "Java Developer", "Google"
        );

        assertEquals("OpenRouter API key is missing or invalid. Set OPENROUTER_API_KEY and restart backend.",
            result.get("feedback"));
    }
}
