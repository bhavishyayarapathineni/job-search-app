package com.jobsearch.service;

import com.jobsearch.model.*;
import com.jobsearch.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("java:S3776")
public class ProfileService {

    private final UserProfileRepository profileRepository;
    private final SavedJobRepository savedJobRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    public UserProfile getProfile(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return profileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserProfile p = new UserProfile();
                    p.setUser(user);
                    return profileRepository.save(p);
                });
    }

    public UserProfile updateProfile(String email, UserProfile updated) {
        User user = userRepository.findByEmail(email).orElseThrow();
        UserProfile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserProfile p = new UserProfile();
                    p.setUser(user);
                    return p;
                });

        if (updated.getHeadline() != null) profile.setHeadline(updated.getHeadline());
        if (updated.getPhone() != null) profile.setPhone(updated.getPhone());
        if (updated.getLocation() != null) profile.setLocation(updated.getLocation());
        if (updated.getLinkedinUrl() != null) profile.setLinkedinUrl(updated.getLinkedinUrl());
        if (updated.getGithubUrl() != null) profile.setGithubUrl(updated.getGithubUrl());
        if (updated.getPortfolioUrl() != null) profile.setPortfolioUrl(updated.getPortfolioUrl());
        if (updated.getSummary() != null) profile.setSummary(updated.getSummary());
        if (updated.getSkills() != null) profile.setSkills(updated.getSkills());
        if (updated.getExperience() != null) profile.setExperience(updated.getExperience());
        if (updated.getEducation() != null) profile.setEducation(updated.getEducation());
        if (updated.getCertifications() != null) profile.setCertifications(updated.getCertifications());
        if (updated.getCurrentTitle() != null) profile.setCurrentTitle(updated.getCurrentTitle());
        if (updated.getCurrentCompany() != null) profile.setCurrentCompany(updated.getCurrentCompany());
        if (updated.getYearsOfExperience() != null) profile.setYearsOfExperience(updated.getYearsOfExperience());
        if (updated.getPreferredJobType() != null) profile.setPreferredJobType(updated.getPreferredJobType());
        if (updated.getPreferredLocation() != null) profile.setPreferredLocation(updated.getPreferredLocation());
        if (updated.getExpectedSalary() != null) profile.setExpectedSalary(updated.getExpectedSalary());
        if (updated.getOpenToRemote() != null) profile.setOpenToRemote(updated.getOpenToRemote());
        if (updated.getOpenToRelocation() != null) profile.setOpenToRelocation(updated.getOpenToRelocation());

        // RESUME FIELDS — these were missing before!
        if (updated.getResumeText() != null && !updated.getResumeText().isEmpty()) {
            profile.setResumeText(updated.getResumeText());
        }
        if (updated.getResumeFileName() != null && !updated.getResumeFileName().isEmpty()) {
            profile.setResumeFileName(updated.getResumeFileName());
        }

        return profileRepository.save(profile);
    }

    public SavedJob saveJob(String email, Long jobId) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Job job = jobRepository.findById(jobId).orElseThrow();
        if (savedJobRepository.existsByUserIdAndJobId(user.getId(), jobId)) {
            return savedJobRepository.findByUserIdAndJobId(user.getId(), jobId).orElseThrow();
        }
        SavedJob saved = new SavedJob();
        saved.setUser(user);
        saved.setJob(job);
        return savedJobRepository.save(saved);
    }

    @Transactional
    public void unsaveJob(String email, Long jobId) {
        User user = userRepository.findByEmail(email).orElseThrow();
        savedJobRepository.deleteByUserIdAndJobId(user.getId(), jobId);
    }

    public List<SavedJob> getSavedJobs(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return savedJobRepository.findByUserId(user.getId());
    }
}
