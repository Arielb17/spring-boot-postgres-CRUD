package com.bezkoder.spring_boot_jpa_postgresql.service;

import java.util.Optional;

import com.bezkoder.spring_boot_jpa_postgresql.dto.AuthorDto;
import com.bezkoder.spring_boot_jpa_postgresql.dto.CourseDto;
import com.bezkoder.spring_boot_jpa_postgresql.dto.TutorialDto;
import com.bezkoder.spring_boot_jpa_postgresql.dto.TutorialDetailDto;

public interface RelationshipService {
    AuthorDto createAuthor(AuthorDto authorDto);
    Optional<AuthorDto> getAuthor(Long id);
    Optional<TutorialDto> setAuthor(Long tutorialId, Long authorId);
    CourseDto createCourse(CourseDto courseDto);
    Optional<CourseDto> getCourse(Long id);
    Optional<TutorialDto> addCourse(Long tutorialId, Long courseId);
    Optional<TutorialDto> removeCourse(Long tutorialId, Long courseId);
    Optional<TutorialDetailDto> createDetail(Long tutorialId, TutorialDetailDto detailDto);
    Optional<TutorialDetailDto> getDetail(Long tutorialId);
}
