package com.jobsearch.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String company;

    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String salary;
    private String jobType;
    private String experienceLevel;
    private String source;
    private String sourceUrl;

    @ElementCollection
    private List<String> skills;

    private boolean isActive = true;

    @Column(updatable = false)
    private LocalDateTime postedAt = LocalDateTime.now();

    private LocalDateTime scrapedAt = LocalDateTime.now();
}
