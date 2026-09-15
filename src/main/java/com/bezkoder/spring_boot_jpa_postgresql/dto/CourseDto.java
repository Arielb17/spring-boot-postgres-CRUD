package com.bezkoder.spring_boot_jpa_postgresql.dto;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CourseDto {

    private Long id;
    private String name;

    @JsonIgnore
    private Set<TutorialDto> tutorials = new HashSet<>();

    public CourseDto() {
    }

    public CourseDto(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Set<TutorialDto> getTutorials() { return tutorials; }
    public void setTutorials(Set<TutorialDto> tutorials) { this.tutorials = tutorials; }
}
