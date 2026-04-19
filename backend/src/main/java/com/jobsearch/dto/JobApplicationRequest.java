package com.jobsearch.dto;
import com.jobsearch.model.JobApplication;
import lombok.Data;

@Data
public class JobApplicationRequest {
    private String jobTitle;
    private String company;
    private String location;
    private String salary;
    private String jobUrl;
    private String notes;
    private JobApplication.ApplicationStatus status = JobApplication.ApplicationStatus.APPLIED;
}
