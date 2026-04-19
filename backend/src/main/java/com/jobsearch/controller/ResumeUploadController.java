package com.jobsearch.controller;

import com.jobsearch.model.UserProfile;
import com.jobsearch.repository.UserProfileRepository;
import com.jobsearch.repository.UserRepository;
import com.jobsearch.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@SuppressWarnings({"java:S5361","java:S4684"})
@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "https://bhavishyayarapathineni.github.io"})
public class ResumeUploadController {

    private final UserProfileRepository profileRepository;
    private final UserRepository userRepository;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadResume(
            Authentication auth,
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> result = new HashMap<>();
        try {
            String filename = file.getOriginalFilename() != null
                    ? file.getOriginalFilename().toLowerCase() : "";
            String contentType = file.getContentType() != null
                    ? file.getContentType().toLowerCase() : "";
            String text = "";

            log.info("Uploading resume: {} type: {}", filename, contentType);

            if (filename.endsWith(".pdf") || contentType.contains("pdf")) {
                // PDFBox 3.x uses Loader.loadPDF
                try (var doc = Loader.loadPDF(file.getBytes())) {
                    var stripper = new PDFTextStripper();
                    text = stripper.getText(doc);
                }
            } else if (filename.endsWith(".docx") || contentType.contains("wordprocessingml")) {
                text = extractDocxText(file.getBytes());
            } else if (filename.endsWith(".doc") || contentType.contains("msword")) {
                text = new String(file.getBytes(), StandardCharsets.UTF_8)
                        .replaceAll("[^\\x20-\\x7E\\n\\r\\t]", " ")
                        .replaceAll("\\s{3,}", "\n").trim();
            } else if (filename.endsWith(".rtf") || contentType.contains("rtf")) {
                text = new String(file.getBytes(), StandardCharsets.UTF_8)
                        .replaceAll("\\\\[a-z]+\\d*\\s?", " ")
                        .replaceAll("[{}]", "")
                        .replaceAll("\\s{2,}", " ").trim();
            } else {
                // TXT or any other format
                text = new String(file.getBytes(), StandardCharsets.UTF_8)
                        .replaceAll("[^\\x20-\\x7E\\n\\r\\t]", " ")
                        .replaceAll("\\s{3,}", "\n").trim();
            }

            text = text.replaceAll("\\r\\n", "\n")
                       .replaceAll("\\r", "\n")
                       .replaceAll("\\n{4,}", "\n\n")
                       .trim();

            if (text.length() < 50) {
                result.put("error", "Could not extract text. Please paste your resume manually.");
                return ResponseEntity.badRequest().body(result);
            }

            // Save to profile
            User user = userRepository.findByEmail(auth.getName()).orElseThrow();
            UserProfile profile = profileRepository.findByUserId(user.getId())
                    .orElseGet(() -> {
                        UserProfile p = new UserProfile();
                        p.setUser(user);
                        return p;
                    });

            profile.setResumeText(text);
            profile.setResumeFileName(file.getOriginalFilename());
            profileRepository.save(profile);

            log.info("Resume saved for: {} chars: {}", auth.getName(), text.length());

            result.put("success", true);
            result.put("filename", file.getOriginalFilename());
            result.put("textLength", text.length());
            result.put("preview", text);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Resume upload error: {}", e.getMessage());
            result.put("error", "Failed: " + e.getMessage() + ". Please paste your resume text manually.");
            return ResponseEntity.badRequest().body(result);
        }
    }

    private String extractDocxText(byte[] bytes) {
        try {
            var zip = new java.util.zip.ZipInputStream(
                    new java.io.ByteArrayInputStream(bytes));
            java.util.zip.ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.getName().equals("word/document.xml")) {
                    String xml = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                    return xml.replaceAll("<[^>]+>", " ")
                              .replaceAll("\\s{2,}", " ").trim();
                }
            }
        } catch (Exception e) {
            log.error("DOCX error: {}", e.getMessage());
        }
        return new String(bytes, StandardCharsets.UTF_8)
                .replaceAll("[^\\x20-\\x7E\\n]", " ").trim();
    }
}
