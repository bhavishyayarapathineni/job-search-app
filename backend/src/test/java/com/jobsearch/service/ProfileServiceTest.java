package com.jobsearch.service;

import com.jobsearch.model.*;
import com.jobsearch.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock private UserProfileRepository profileRepository;
    @Mock private SavedJobRepository savedJobRepository;
    @Mock private UserRepository userRepository;
    @Mock private JobRepository jobRepository;
    @InjectMocks private ProfileService profileService;

    private User testUser;
    private UserProfile testProfile;
    private Job testJob;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@gmail.com");
        testUser.setFullName("Test User");

        testProfile = new UserProfile();
        testProfile.setId(1L);
        testProfile.setUser(testUser);
        testProfile.setHeadline("Java Developer");
        testProfile.setResumeText("My resume text");

        testJob = new Job();
        testJob.setId(1L);
        testJob.setTitle("Java Developer");
        testJob.setCompany("Google");
    }

    @Test
    void getProfile_ExistingProfile_ReturnsProfile() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(testUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        UserProfile result = profileService.getProfile("test@gmail.com");
        assertNotNull(result);
        assertEquals("Java Developer", result.getHeadline());
    }

    @Test
    void getProfile_NewUser_CreatesEmptyProfile() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(testUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(profileRepository.save(any(UserProfile.class))).thenReturn(testProfile);
        UserProfile result = profileService.getProfile("test@gmail.com");
        assertNotNull(result);
        verify(profileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void getProfile_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail("notfound@gmail.com")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> profileService.getProfile("notfound@gmail.com"));
    }

    @Test
    void updateProfile_UpdatesResumeText() {
        UserProfile updated = new UserProfile();
        updated.setResumeText("Updated resume text");
        updated.setHeadline("Sr. Java Developer");
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(testUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(profileRepository.save(any(UserProfile.class))).thenReturn(testProfile);
        UserProfile result = profileService.updateProfile("test@gmail.com", updated);
        assertNotNull(result);
        verify(profileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void updateProfile_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail("notfound@gmail.com")).thenReturn(Optional.empty());
        UserProfile empty = new UserProfile();
        assertThrows(RuntimeException.class, () -> profileService.updateProfile("notfound@gmail.com", empty));
    }

    @Test
    void saveJob_NewJob_SavesSuccessfully() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(testUser));
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));
        when(savedJobRepository.existsByUserIdAndJobId(1L, 1L)).thenReturn(false);
        when(savedJobRepository.save(any(SavedJob.class))).thenReturn(new SavedJob());
        SavedJob result = profileService.saveJob("test@gmail.com", 1L);
        assertNotNull(result);
        verify(savedJobRepository, times(1)).save(any(SavedJob.class));
    }

    @Test
    void saveJob_AlreadySaved_ReturnsExisting() {
        SavedJob existing = new SavedJob();
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(testUser));
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));
        when(savedJobRepository.existsByUserIdAndJobId(1L, 1L)).thenReturn(true);
        when(savedJobRepository.findByUserIdAndJobId(1L, 1L)).thenReturn(Optional.of(existing));
        SavedJob result = profileService.saveJob("test@gmail.com", 1L);
        assertNotNull(result);
        verify(savedJobRepository, never()).save(any(SavedJob.class));
    }

    @Test
    void saveJob_JobNotFound_ThrowsException() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(testUser));
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> profileService.saveJob("test@gmail.com", 99L));
    }
    @Test
    void unsaveJob_RemovesJob() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(testUser));
        doNothing().when(savedJobRepository).deleteByUserIdAndJobId(1L, 1L);
        profileService.unsaveJob("test@gmail.com", 1L);
        verify(savedJobRepository, times(1)).deleteByUserIdAndJobId(1L, 1L);
    }

    @Test
    void unsaveJob_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail("notfound@gmail.com")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> profileService.unsaveJob("notfound@gmail.com", 1L));
    }

    @Test
    void getSavedJobs_ReturnsList() {
        SavedJob savedJob = new SavedJob();
        savedJob.setJob(testJob);
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(testUser));
        when(savedJobRepository.findByUserId(1L)).thenReturn(java.util.List.of(savedJob));
        var result = profileService.getSavedJobs("test@gmail.com");
        assertEquals(1, result.size());
    }

}
