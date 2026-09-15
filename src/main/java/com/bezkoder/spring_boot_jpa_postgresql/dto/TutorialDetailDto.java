package com.bezkoder.spring_boot_jpa_postgresql.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;

public class TutorialDetailDto {

    private Long id;
    private String content;
    private Integer estimatedMinutes;

    @JsonBackReference("tutorial-detail")
    private TutorialDto tutorial;

    public TutorialDetailDto() {
    }

    public TutorialDetailDto(String content, Integer estimatedMinutes) {
        this.content = content;
        this.estimatedMinutes = estimatedMinutes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(Integer estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }
    public TutorialDto getTutorial() { return tutorial; }
    public void setTutorial(TutorialDto tutorial) { this.tutorial = tutorial; }
}
