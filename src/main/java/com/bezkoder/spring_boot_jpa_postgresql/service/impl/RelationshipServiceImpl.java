package com.bezkoder.spring_boot_jpa_postgresql.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bezkoder.spring_boot_jpa_postgresql.dto.AuthorDto;
import com.bezkoder.spring_boot_jpa_postgresql.dto.CourseDto;
import com.bezkoder.spring_boot_jpa_postgresql.dto.TutorialDto;
import com.bezkoder.spring_boot_jpa_postgresql.dto.TutorialDetailDto;
import com.bezkoder.spring_boot_jpa_postgresql.mapper.AuthorMapper;
import com.bezkoder.spring_boot_jpa_postgresql.mapper.CourseMapper;
import com.bezkoder.spring_boot_jpa_postgresql.mapper.TutorialMapper;
import com.bezkoder.spring_boot_jpa_postgresql.mapper.TutorialDetailMapper;
import com.bezkoder.spring_boot_jpa_postgresql.model.Author;
import com.bezkoder.spring_boot_jpa_postgresql.model.Course;
import com.bezkoder.spring_boot_jpa_postgresql.model.Tutorial;
import com.bezkoder.spring_boot_jpa_postgresql.model.TutorialDetail;
import com.bezkoder.spring_boot_jpa_postgresql.repository.AuthorRepository;
import com.bezkoder.spring_boot_jpa_postgresql.repository.CourseRepository;
import com.bezkoder.spring_boot_jpa_postgresql.repository.TutorialDetailRepository;
import com.bezkoder.spring_boot_jpa_postgresql.repository.TutorialRepository;
import com.bezkoder.spring_boot_jpa_postgresql.service.RelationshipService;

@Service
public class RelationshipServiceImpl implements RelationshipService {
    private final AuthorRepository authorRepository;
    private final CourseRepository courseRepository;
    private final TutorialRepository tutorialRepository;
    private final TutorialDetailRepository detailRepository;
    private final AuthorMapper authorMapper;
    private final CourseMapper courseMapper;
    private final TutorialMapper tutorialMapper;
    private final TutorialDetailMapper detailMapper;

    public RelationshipServiceImpl(AuthorRepository authorRepository, CourseRepository courseRepository,
            TutorialRepository tutorialRepository, TutorialDetailRepository detailRepository,
            AuthorMapper authorMapper, CourseMapper courseMapper, TutorialMapper tutorialMapper,
            TutorialDetailMapper detailMapper) {
        this.authorRepository = authorRepository;
        this.courseRepository = courseRepository;
        this.tutorialRepository = tutorialRepository;
        this.detailRepository = detailRepository;
        this.authorMapper = authorMapper;
        this.courseMapper = courseMapper;
        this.tutorialMapper = tutorialMapper;
        this.detailMapper = detailMapper;
    }

    @Override
    @Transactional
    public AuthorDto createAuthor(AuthorDto authorDto) {
        Author author = authorMapper.toEntity(authorDto);
        return authorMapper.toDto(authorRepository.save(author));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthorDto> getAuthor(Long id) {
        return authorRepository.findById(id).map(authorMapper::toDto);
    }

    @Override
    @Transactional
    public Optional<TutorialDto> setAuthor(Long tutorialId, Long authorId) {
        Optional<Tutorial> tutorial = tutorialRepository.findById(tutorialId);
        Optional<Author> author = authorRepository.findById(authorId);
        if (tutorial.isEmpty() || author.isEmpty()) return Optional.empty();
        tutorial.get().setAuthor(author.get());
        return Optional.of(tutorialMapper.toDto(tutorialRepository.save(tutorial.get())));
    }

    @Override
    @Transactional
    public CourseDto createCourse(CourseDto courseDto) {
        Course course = courseMapper.toEntity(courseDto);
        return courseMapper.toDto(courseRepository.save(course));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CourseDto> getCourse(Long id) {
        return courseRepository.findById(id).map(courseMapper::toDto);
    }

    @Override
    @Transactional
    public Optional<TutorialDto> addCourse(Long tutorialId, Long courseId) {
        Optional<Tutorial> tutorial = tutorialRepository.findById(tutorialId);
        Optional<Course> course = courseRepository.findById(courseId);
        if (tutorial.isEmpty() || course.isEmpty()) return Optional.empty();
        tutorial.get().addCourse(course.get());
        return Optional.of(tutorialMapper.toDto(tutorialRepository.save(tutorial.get())));
    }

    @Override
    @Transactional
    public Optional<TutorialDto> removeCourse(Long tutorialId, Long courseId) {
        Optional<Tutorial> tutorial = tutorialRepository.findById(tutorialId);
        Optional<Course> course = courseRepository.findById(courseId);
        if (tutorial.isEmpty() || course.isEmpty()) return Optional.empty();
        tutorial.get().removeCourse(course.get());
        return Optional.of(tutorialMapper.toDto(tutorialRepository.save(tutorial.get())));
    }

    @Override
    @Transactional
    public Optional<TutorialDetailDto> createDetail(Long tutorialId, TutorialDetailDto detailDto) {
        Optional<Tutorial> tutorial = tutorialRepository.findById(tutorialId);
        if (tutorial.isEmpty()) return Optional.empty();
        TutorialDetail detail = detailMapper.toEntity(detailDto);
        tutorial.get().setDetail(detail);
        Tutorial savedTutorial = tutorialRepository.save(tutorial.get());
        return Optional.of(detailMapper.toDto(savedTutorial.getDetail()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TutorialDetailDto> getDetail(Long tutorialId) {
        return detailRepository.findByTutorialId(tutorialId).map(detailMapper::toDto);
    }
}
