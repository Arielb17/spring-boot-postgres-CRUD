package com.bezkoder.spring_boot_jpa_postgresql.dto;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

public class TutorialDto {

    private Long id;
    private String title;
    private String description;
    private boolean published;

    @JsonBackReference("author-tutorials")
    private AuthorDto author;

    private Set<CourseDto> courses = new HashSet<>();

    @JsonManagedReference("tutorial-detail")
    private TutorialDetailDto detail;

    public TutorialDto() {
    }

    public TutorialDto(String title, String description, boolean published) {
        this.title = title;
        this.description = description;
        this.published = published;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
    public AuthorDto getAuthor() { return author; }
    public void setAuthor(AuthorDto author) { this.author = author; }
    public Set<CourseDto> getCourses() { return courses; }
    public void setCourses(Set<CourseDto> courses) { this.courses = courses; }
    public TutorialDetailDto getDetail() { return detail; }
    public void setDetail(TutorialDetailDto detail) { this.detail = detail; }
}
