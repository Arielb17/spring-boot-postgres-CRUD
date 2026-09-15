package com.bezkoder.spring_boot_jpa_postgresql.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bezkoder.spring_boot_jpa_postgresql.dto.AuthorDto;
import com.bezkoder.spring_boot_jpa_postgresql.dto.CourseDto;
import com.bezkoder.spring_boot_jpa_postgresql.dto.TutorialDto;
import com.bezkoder.spring_boot_jpa_postgresql.dto.TutorialDetailDto;
import com.bezkoder.spring_boot_jpa_postgresql.exception.ResourceNotFoundException;
import com.bezkoder.spring_boot_jpa_postgresql.mapper.AuthorMapper;
import com.bezkoder.spring_boot_jpa_postgresql.mapper.CourseMapper;
import com.bezkoder.spring_boot_jpa_postgresql.mapper.TutorialDetailMapper;
import com.bezkoder.spring_boot_jpa_postgresql.mapper.TutorialMapper;
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

    private static final String AUTHOR_NOT_FOUND = "Autor não encontrado.";
    private static final String COURSE_NOT_FOUND = "Curso não encontrado.";
    private static final String DETAIL_NOT_FOUND = "Detalhe do tutorial não encontrado.";
    private static final String TUTORIAL_NOT_FOUND = "Tutorial não encontrado.";
    private static final String TUTORIAL_COURSE_NOT_FOUND = "Curso não está associado ao tutorial.";

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
    public AuthorDto getAuthor(Long id) {
        return authorMapper.toDto(findAuthor(id));
    }

    @Override
    @Transactional
    public TutorialDto setAuthor(Long tutorialId, Long authorId) {
        Tutorial tutorial = findTutorial(tutorialId);
        Author author = findAuthor(authorId);
        tutorial.setAuthor(author);
        return tutorialMapper.toDto(tutorialRepository.save(tutorial));
    }

    @Override
    @Transactional
    public CourseDto createCourse(CourseDto courseDto) {
        Course course = courseMapper.toEntity(courseDto);
        return courseMapper.toDto(courseRepository.save(course));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDto getCourse(Long id) {
        return courseMapper.toDto(findCourse(id));
    }

    @Override
    @Transactional
    public TutorialDto addCourse(Long tutorialId, Long courseId) {
        Tutorial tutorial = findTutorial(tutorialId);
        Course course = findCourse(courseId);
        tutorial.addCourse(course);
        return tutorialMapper.toDto(tutorialRepository.save(tutorial));
    }

    @Override
    @Transactional
    public void removeCourse(Long tutorialId, Long courseId) {
        Tutorial tutorial = findTutorial(tutorialId);
        Course course = findCourse(courseId);
        if (!tutorial.removeCourse(course)) {
            throw new ResourceNotFoundException(TUTORIAL_COURSE_NOT_FOUND);
        }
        tutorialRepository.save(tutorial);
    }

    @Override
    @Transactional
    public TutorialDetailDto createDetail(Long tutorialId, TutorialDetailDto detailDto) {
        Tutorial tutorial = findTutorial(tutorialId);
        TutorialDetail detail = detailMapper.toEntity(detailDto);
        tutorial.setDetail(detail);
        Tutorial savedTutorial = tutorialRepository.save(tutorial);
        return detailMapper.toDto(savedTutorial.getDetail());
    }

    @Override
    @Transactional(readOnly = true)
    public TutorialDetailDto getDetail(Long tutorialId) {
        TutorialDetail detail = detailRepository.findByTutorialId(tutorialId)
                .orElseThrow(() -> new ResourceNotFoundException(DETAIL_NOT_FOUND));
        return detailMapper.toDto(detail);
    }

    private Author findAuthor(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(AUTHOR_NOT_FOUND));
    }

    private Course findCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(COURSE_NOT_FOUND));
    }

    private Tutorial findTutorial(Long id) {
        return tutorialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TUTORIAL_NOT_FOUND));
    }
}
