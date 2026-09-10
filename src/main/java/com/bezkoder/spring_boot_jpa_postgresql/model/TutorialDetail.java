package com.bezkoder.spring_boot_jpa_postgresql.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tutorial_details")
public class TutorialDetail {

    @OneToOne
    @JoinColumn(name = "tutorial_id", unique = true)
    @JsonBackReference("tutorial-detail")
    private Tutorial tutorial;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String content;
    private Integer estimatedMinutes;

    public TutorialDetail() {
    }

    public TutorialDetail(String content, Integer estimatedMinutes) {
        this.content = content;
        this.estimatedMinutes = estimatedMinutes;
    }

    public Tutorial getTutorial() { return tutorial; }
    public void setTutorial(Tutorial tutorial) { this.tutorial = tutorial; }
    public Long getId() { return id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(Integer estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }
}
