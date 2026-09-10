package com.bezkoder.spring_boot_jpa_postgresql.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public RelationshipServiceImpl(AuthorRepository authorRepository, CourseRepository courseRepository,
            TutorialRepository tutorialRepository, TutorialDetailRepository detailRepository) {
        this.authorRepository = authorRepository;
        this.courseRepository = courseRepository;
        this.tutorialRepository = tutorialRepository;
        this.detailRepository = detailRepository;
    }

    @Override
    public Author createAuthor(String name) { return authorRepository.save(new Author(name)); }

    @Override
    public Optional<Author> getAuthor(Long id) { return authorRepository.findById(id); }

    @Override
    @Transactional
    public Optional<Tutorial> setAuthor(Long tutorialId, Long authorId) {
        Optional<Tutorial> tutorial = tutorialRepository.findById(tutorialId);
        Optional<Author> author = authorRepository.findById(authorId);
        if (tutorial.isEmpty() || author.isEmpty()) return Optional.empty();
        tutorial.get().setAuthor(author.get());
        return Optional.of(tutorialRepository.save(tutorial.get()));
    }

    @Override
    public Course createCourse(String name) { return courseRepository.save(new Course(name)); }

    @Override
    public Optional<Course> getCourse(Long id) { return courseRepository.findById(id); }

    @Override
    @Transactional
    public Optional<Tutorial> addCourse(Long tutorialId, Long courseId) {
        Optional<Tutorial> tutorial = tutorialRepository.findById(tutorialId);
        Optional<Course> course = courseRepository.findById(courseId);
        if (tutorial.isEmpty() || course.isEmpty()) return Optional.empty();
        tutorial.get().addCourse(course.get());
        return Optional.of(tutorialRepository.save(tutorial.get()));
    }

    @Override
    @Transactional
    public Optional<Tutorial> removeCourse(Long tutorialId, Long courseId) {
        Optional<Tutorial> tutorial = tutorialRepository.findById(tutorialId);
        Optional<Course> course = courseRepository.findById(courseId);
        if (tutorial.isEmpty() || course.isEmpty()) return Optional.empty();
        tutorial.get().removeCourse(course.get());
        return Optional.of(tutorialRepository.save(tutorial.get()));
    }

    @Override
    @Transactional
    public Optional<TutorialDetail> createDetail(Long tutorialId, String content, Integer estimatedMinutes) {
        Optional<Tutorial> tutorial = tutorialRepository.findById(tutorialId);
        if (tutorial.isEmpty()) return Optional.empty();
        TutorialDetail detail = new TutorialDetail(content, estimatedMinutes);
        tutorial.get().setDetail(detail);
        Tutorial savedTutorial = tutorialRepository.save(tutorial.get());
        return Optional.of(savedTutorial.getDetail());
    }

    @Override
    public Optional<TutorialDetail> getDetail(Long tutorialId) { return detailRepository.findByTutorialId(tutorialId); }
}
