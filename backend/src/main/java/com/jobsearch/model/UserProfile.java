package com.jobsearch.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String headline;
    private String phone;
    private String location;
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(columnDefinition = "TEXT")
    private String experience;

    @Column(columnDefinition = "TEXT")
    private String education;

    @Column(columnDefinition = "TEXT")
    private String certifications;

    private String resumeFileName;

    @Column(columnDefinition = "TEXT")
    private String resumeText;

    private Integer yearsOfExperience;
    private String currentTitle;
    private String currentCompany;
    private String preferredJobType;
    private String preferredLocation;
    private Integer expectedSalary;
    private Boolean openToRemote = true;
    private Boolean openToRelocation = false;
}
