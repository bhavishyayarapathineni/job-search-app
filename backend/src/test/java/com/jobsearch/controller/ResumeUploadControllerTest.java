package com.jobsearch.controller;

import com.jobsearch.model.User;
import com.jobsearch.model.UserProfile;
import com.jobsearch.repository.UserProfileRepository;
import com.jobsearch.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;

import java.lang.reflect.Method;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumeUploadControllerTest {

    @Mock private UserProfileRepository profileRepository;
    @Mock private UserRepository userRepository;
    @Mock private Authentication authentication;

    @InjectMocks private ResumeUploadController controller;

    @Test
    void uploadResume_ReturnsBadRequestForShortExtractedText() {
        MockMultipartFile file = new MockMultipartFile(
            "file", "resume.txt", "text/plain", "too short".getBytes(StandardCharsets.UTF_8));

        ResponseEntity<Map<String, Object>> response = controller.uploadResume(authentication, file);

        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().containsKey("error"));
    }

    @Test
    void uploadResume_SavesProfileForTxtFile() {
        String longText = "Java Spring Boot Microservices AWS Docker Kubernetes REST APIs experience "
            + "with enterprise systems and scalable backend services across multiple teams.";
        MockMultipartFile file = new MockMultipartFile(
            "file", "resume.txt", "text/plain", longText.getBytes(StandardCharsets.UTF_8));

        User user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");

        when(authentication.getName()).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(profileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Map<String, Object>> response = controller.uploadResume(authentication, file);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(true, response.getBody().get("success"));

        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(profileRepository).save(captor.capture());
        assertEquals("resume.txt", captor.getValue().getResumeFileName());
        assertTrue(captor.getValue().getResumeText().contains("Java Spring Boot"));
    }

    @Test
    void uploadResume_SavesProfileForDocFile() {
        String longDoc = "Experienced Java developer with Spring Boot microservices AWS Docker Kubernetes "
            + "REST API design and scalable backend architecture across enterprise systems.";
        MockMultipartFile file = new MockMultipartFile(
            "file", "resume.doc", "application/msword", longDoc.getBytes(StandardCharsets.UTF_8));

        User user = new User();
        user.setId(2L);
        user.setEmail("doc@test.com");
        UserProfile existing = new UserProfile();
        existing.setUser(user);

        when(authentication.getName()).thenReturn("doc@test.com");
        when(userRepository.findByEmail("doc@test.com")).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(2L)).thenReturn(Optional.of(existing));
        when(profileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Map<String, Object>> response = controller.uploadResume(authentication, file);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(true, response.getBody().get("success"));
    }

    @Test
    void uploadResume_SavesProfileForRtfFile() {
        String rtf = "{\\rtf1\\ansi Java Developer with Spring Boot and AWS experience in enterprise systems}";
        rtf = rtf + " and containerized deployment with Docker Kubernetes across teams and products.";
        MockMultipartFile file = new MockMultipartFile(
            "file", "resume.rtf", "application/rtf", rtf.getBytes(StandardCharsets.UTF_8));

        User user = new User();
        user.setId(3L);
        user.setEmail("rtf@test.com");

        when(authentication.getName()).thenReturn("rtf@test.com");
        when(userRepository.findByEmail("rtf@test.com")).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(3L)).thenReturn(Optional.empty());
        when(profileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Map<String, Object>> response = controller.uploadResume(authentication, file);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(true, response.getBody().get("success"));
    }

    @Test
    void uploadResume_ReturnsBadRequestWhenPdfParsingFails() {
        byte[] invalidPdf = "not-a-real-pdf".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile(
            "file", "resume.pdf", "application/pdf", invalidPdf);

        ResponseEntity<Map<String, Object>> response = controller.uploadResume(authentication, file);

        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().containsKey("error"));
    }

    @Test
    void extractDocxText_ReadsWordDocumentXml() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry("word/document.xml"));
            zos.write("<w:p><w:t>Java Developer Resume</w:t></w:p>".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        Method method = ResumeUploadController.class.getDeclaredMethod("extractDocxText", byte[].class);
        method.setAccessible(true);
        String text = (String) method.invoke(controller, (Object) baos.toByteArray());

        assertTrue(text.contains("Java Developer Resume"));
    }
}
