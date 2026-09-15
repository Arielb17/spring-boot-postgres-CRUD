package com.bezkoder.spring_boot_jpa_postgresql.service;

import com.bezkoder.spring_boot_jpa_postgresql.dto.AuthorDto;
import com.bezkoder.spring_boot_jpa_postgresql.dto.CourseDto;
import com.bezkoder.spring_boot_jpa_postgresql.dto.TutorialDto;
import com.bezkoder.spring_boot_jpa_postgresql.dto.TutorialDetailDto;

public interface RelationshipService {

    AuthorDto createAuthor(AuthorDto authorDto);

    AuthorDto getAuthor(Long id);

    TutorialDto setAuthor(Long tutorialId, Long authorId);

    CourseDto createCourse(CourseDto courseDto);

    CourseDto getCourse(Long id);

    TutorialDto addCourse(Long tutorialId, Long courseId);

    void removeCourse(Long tutorialId, Long courseId);

    TutorialDetailDto createDetail(Long tutorialId, TutorialDetailDto detailDto);

    TutorialDetailDto getDetail(Long tutorialId);
}
