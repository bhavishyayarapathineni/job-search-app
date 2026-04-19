package com.jobsearch.service;
import com.jobsearch.model.UserProfile;
import com.jobsearch.repository.UserProfileRepository;
import com.jobsearch.repository.UserRepository;
import com.jobsearch.model.User;
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
        String result = coverLetterService.generate("Java Developer", "Google", "test@gmail.com");
        assertNotNull(result);
        assertTrue(result.contains("Google"));
        assertTrue(result.length() > 100);
    }

    @Test
    void generate_WithProfile_ReturnsTemplate() {
        User user = new User();
        user.setId(1L);
        UserProfile profile = new UserProfile();
        profile.setResumeText("Java developer with 3 years experience");
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        String result = coverLetterService.generate("Software Engineer", "Amazon", "test@gmail.com");
        assertNotNull(result);
        assertTrue(result.contains("Amazon"));
    }

    @Test
    void generate_WithNullResumeText_ReturnsTemplate() {
        User user = new User();
        user.setId(1L);
        UserProfile profile = new UserProfile();
        profile.setResumeText(null);
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        String result = coverLetterService.generate("Engineer", "Microsoft", "test@gmail.com");
        assertNotNull(result);
        assertTrue(result.contains("Microsoft"));
    }

    @Test
    void generate_ContainsSenderInfo() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.empty());
        String result = coverLetterService.generate("Developer", "Apple", "test@gmail.com");
        assertTrue(result.contains("Bhavishya"));
    }
}
