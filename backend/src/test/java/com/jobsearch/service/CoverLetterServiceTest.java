package com.jobsearch.service;

import com.jobsearch.repository.UserProfileRepository;
import com.jobsearch.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoverLetterServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserProfileRepository profileRepository;
    @InjectMocks private CoverLetterService coverLetterService;

    @Test
    void generate_NoProfile_ReturnsTemplate() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.empty());
        String result = coverLetterService.generate(
            "Java Developer", "Google", "test@gmail.com"
        );
        assertNotNull(result);
        assertTrue(result.contains("Google"));
        assertTrue(result.length() > 100);
    }

    @Test
    void generate_NotNull() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.empty());
        String result = coverLetterService.generate(
            "Software Engineer", "Amazon", "test@gmail.com"
        );
        assertNotNull(result);
    }
}
