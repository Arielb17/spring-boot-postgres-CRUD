package com.bezkoder.spring_boot_jpa_postgresql.service;

import java.util.Optional;

import com.bezkoder.spring_boot_jpa_postgresql.model.Author;
import com.bezkoder.spring_boot_jpa_postgresql.model.Course;
import com.bezkoder.spring_boot_jpa_postgresql.model.Tutorial;
import com.bezkoder.spring_boot_jpa_postgresql.model.TutorialDetail;

public interface RelationshipService {
    Author createAuthor(String name);
    Optional<Author> getAuthor(Long id);
    Optional<Tutorial> setAuthor(Long tutorialId, Long authorId);
    Course createCourse(String name);
    Optional<Course> getCourse(Long id);
    Optional<Tutorial> addCourse(Long tutorialId, Long courseId);
    Optional<Tutorial> removeCourse(Long tutorialId, Long courseId);
    Optional<TutorialDetail> createDetail(Long tutorialId, String content, Integer estimatedMinutes);
    Optional<TutorialDetail> getDetail(Long tutorialId);
}
