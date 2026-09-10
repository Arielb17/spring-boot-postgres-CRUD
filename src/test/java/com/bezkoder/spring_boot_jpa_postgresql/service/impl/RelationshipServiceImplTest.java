package com.bezkoder.spring_boot_jpa_postgresql.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.bezkoder.spring_boot_jpa_postgresql.dto.AuthorDto;
import com.bezkoder.spring_boot_jpa_postgresql.dto.CourseDto;
import com.bezkoder.spring_boot_jpa_postgresql.dto.TutorialDetailDto;
import com.bezkoder.spring_boot_jpa_postgresql.dto.TutorialDto;
import com.bezkoder.spring_boot_jpa_postgresql.mapper.AuthorMapperImpl;
import com.bezkoder.spring_boot_jpa_postgresql.mapper.CourseMapperImpl;
import com.bezkoder.spring_boot_jpa_postgresql.mapper.TutorialDetailMapperImpl;
import com.bezkoder.spring_boot_jpa_postgresql.mapper.TutorialMapperImpl;
import com.bezkoder.spring_boot_jpa_postgresql.model.Author;
import com.bezkoder.spring_boot_jpa_postgresql.model.Course;
import com.bezkoder.spring_boot_jpa_postgresql.model.Tutorial;
import com.bezkoder.spring_boot_jpa_postgresql.model.TutorialDetail;
import com.bezkoder.spring_boot_jpa_postgresql.repository.AuthorRepository;
import com.bezkoder.spring_boot_jpa_postgresql.repository.CourseRepository;
import com.bezkoder.spring_boot_jpa_postgresql.repository.TutorialDetailRepository;
import com.bezkoder.spring_boot_jpa_postgresql.repository.TutorialRepository;

class RelationshipServiceImplTest {

    private AuthorRepository authorRepository;
    private CourseRepository courseRepository;
    private TutorialRepository tutorialRepository;
    private TutorialDetailRepository detailRepository;
    private RelationshipServiceImpl relationshipService;

    @BeforeEach
    void setUp() {
        authorRepository = mock(AuthorRepository.class);
        courseRepository = mock(CourseRepository.class);
        tutorialRepository = mock(TutorialRepository.class);
        detailRepository = mock(TutorialDetailRepository.class);
        CourseMapperImpl courseMapper = new CourseMapperImpl();
        TutorialDetailMapperImpl detailMapper = new TutorialDetailMapperImpl();
        TutorialMapperImpl tutorialMapper = new TutorialMapperImpl(courseMapper, detailMapper);
        AuthorMapperImpl authorMapper = new AuthorMapperImpl(tutorialMapper);
        relationshipService = new RelationshipServiceImpl(authorRepository, courseRepository,
                tutorialRepository, detailRepository, authorMapper, courseMapper, tutorialMapper, detailMapper);
    }

    @Test
    void createAuthorMapsNameAndReturnsGeneratedIdWithoutCreatingSubmittedTutorials() {
        AuthorDto request = new AuthorDto("Ana");
        request.setId(999L);
        request.setTutorials(Set.of(new TutorialDto("Submitted", "Nested tutorial", true)));
        when(authorRepository.save(any(Author.class))).thenAnswer(invocation -> {
            Author entity = invocation.getArgument(0);
            assertThat(entity.getId()).isNull();
            assertThat(entity.getTutorials()).isEmpty();
            ReflectionTestUtils.setField(entity, "id", 10L);
            return entity;
        });

        AuthorDto result = relationshipService.createAuthor(request);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Ana");
        assertThat(result.getTutorials()).isEmpty();
        assertThat(request.getId()).isEqualTo(999L);
    }

    @Test
    void createCourseMapsNameAndReturnsGeneratedIdWithoutCreatingSubmittedTutorials() {
        CourseDto request = new CourseDto("Spring");
        request.setId(999L);
        request.setTutorials(Set.of(new TutorialDto("Submitted", "Nested tutorial", true)));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course entity = invocation.getArgument(0);
            assertThat(entity.getId()).isNull();
            assertThat(entity.getTutorials()).isEmpty();
            ReflectionTestUtils.setField(entity, "id", 20L);
            return entity;
        });

        CourseDto result = relationshipService.createCourse(request);

        assertThat(result.getId()).isEqualTo(20L);
        assertThat(result.getName()).isEqualTo("Spring");
        assertThat(request.getId()).isEqualTo(999L);
    }

    @Test
    void setAuthorKeepsBothSidesConsistentAndReturnsTutorialDto() {
        Tutorial tutorial = tutorial(1L);
        Author previousAuthor = new Author("Previous");
        Author nextAuthor = new Author("Next");
        tutorial.setAuthor(previousAuthor);
        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(tutorial));
        when(authorRepository.findById(2L)).thenReturn(Optional.of(nextAuthor));
        when(tutorialRepository.save(tutorial)).thenReturn(tutorial);

        TutorialDto result = relationshipService.setAuthor(1L, 2L).orElseThrow();

        assertThat(tutorial.getAuthor()).isSameAs(nextAuthor);
        assertThat(previousAuthor.getTutorials()).isEmpty();
        assertThat(nextAuthor.getTutorials()).containsExactly(tutorial);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Spring");
    }

    @Test
    void addAndRemoveCourseKeepBothSidesConsistentAndReturnUpdatedDtos() {
        Tutorial tutorial = tutorial(1L);
        Course course = new Course("Java");
        ReflectionTestUtils.setField(course, "id", 2L);
        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(tutorial));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(tutorialRepository.save(tutorial)).thenReturn(tutorial);

        TutorialDto added = relationshipService.addCourse(1L, 2L).orElseThrow();

        assertThat(tutorial.getCourses()).containsExactly(course);
        assertThat(course.getTutorials()).containsExactly(tutorial);
        assertThat(added.getCourses()).singleElement().satisfies(mappedCourse -> {
            assertThat(mappedCourse.getId()).isEqualTo(2L);
            assertThat(mappedCourse.getName()).isEqualTo("Java");
        });

        TutorialDto removed = relationshipService.removeCourse(1L, 2L).orElseThrow();

        assertThat(tutorial.getCourses()).isEmpty();
        assertThat(course.getTutorials()).isEmpty();
        assertThat(removed.getCourses()).isEmpty();
        assertThat(added.getCourses()).hasSize(1);
    }

    @Test
    void createDetailUsesTutorialFromPathAndReplacesExistingDetailThroughEntityHelper() {
        Tutorial tutorial = tutorial(1L);
        TutorialDetail previousDetail = new TutorialDetail("Previous", 5);
        tutorial.setDetail(previousDetail);
        TutorialDetailDto request = new TutorialDetailDto("Updated", 15);
        request.setId(999L);
        TutorialDto submittedTutorial = new TutorialDto("Submitted", "Unrelated", false);
        submittedTutorial.setId(999L);
        request.setTutorial(submittedTutorial);
        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(tutorial));
        when(tutorialRepository.save(tutorial)).thenAnswer(invocation -> {
            assertThat(tutorial.getDetail().getId()).isNull();
            ReflectionTestUtils.setField(tutorial.getDetail(), "id", 30L);
            return tutorial;
        });

        TutorialDetailDto result = relationshipService.createDetail(1L, request).orElseThrow();

        assertThat(tutorial.getDetail().getTutorial()).isSameAs(tutorial);
        assertThat(previousDetail.getTutorial()).isNull();
        assertThat(result.getId()).isEqualTo(30L);
        assertThat(result.getContent()).isEqualTo("Updated");
        assertThat(result.getEstimatedMinutes()).isEqualTo(15);
        assertThat(result.getTutorial()).isNull();
        verify(tutorialRepository, never()).findById(999L);
    }

    @Test
    void readsReturnDtosForAuthorGraphCourseAndDetail() {
        Author author = new Author("Ana");
        ReflectionTestUtils.setField(author, "id", 2L);
        Tutorial tutorial = tutorial(1L);
        tutorial.setAuthor(author);
        Course course = new Course("Java");
        ReflectionTestUtils.setField(course, "id", 3L);
        tutorial.addCourse(course);
        TutorialDetail detail = new TutorialDetail("Content", 20);
        ReflectionTestUtils.setField(detail, "id", 4L);
        tutorial.setDetail(detail);
        when(authorRepository.findById(2L)).thenReturn(Optional.of(author));
        when(courseRepository.findById(3L)).thenReturn(Optional.of(course));
        when(detailRepository.findByTutorialId(1L)).thenReturn(Optional.of(detail));

        AuthorDto mappedAuthor = relationshipService.getAuthor(2L).orElseThrow();
        CourseDto mappedCourse = relationshipService.getCourse(3L).orElseThrow();
        TutorialDetailDto mappedDetail = relationshipService.getDetail(1L).orElseThrow();

        assertThat(mappedAuthor.getId()).isEqualTo(2L);
        assertThat(mappedAuthor.getTutorials()).singleElement().satisfies(mappedTutorial -> {
            assertThat(mappedTutorial.getId()).isEqualTo(1L);
            assertThat(mappedTutorial.getAuthor()).isNull();
            assertThat(mappedTutorial.getCourses()).extracting(CourseDto::getId).containsExactly(3L);
            assertThat(mappedTutorial.getDetail().getContent()).isEqualTo("Content");
        });
        assertThat(mappedCourse.getName()).isEqualTo("Java");
        assertThat(mappedDetail.getId()).isEqualTo(4L);
        assertThat(mappedDetail.getTutorial()).isNull();
        mappedAuthor.setName("Edited DTO");
        mappedDetail.setContent("Edited DTO");
        assertThat(author.getName()).isEqualTo("Ana");
        assertThat(detail.getContent()).isEqualTo("Content");
    }

    @Test
    void missingResourcesKeepEmptyOptionalsAndDoNotSave() {
        when(tutorialRepository.findById(1L)).thenReturn(Optional.empty());
        when(authorRepository.findById(2L)).thenReturn(Optional.empty());
        when(courseRepository.findById(3L)).thenReturn(Optional.empty());
        when(detailRepository.findByTutorialId(1L)).thenReturn(Optional.empty());

        assertThat(relationshipService.getAuthor(2L)).isEmpty();
        assertThat(relationshipService.getCourse(3L)).isEmpty();
        assertThat(relationshipService.getDetail(1L)).isEmpty();
        assertThat(relationshipService.setAuthor(1L, 2L)).isEmpty();
        assertThat(relationshipService.addCourse(1L, 3L)).isEmpty();
        assertThat(relationshipService.removeCourse(1L, 3L)).isEmpty();
        assertThat(relationshipService.createDetail(1L, new TutorialDetailDto("Content", 20))).isEmpty();
        verify(tutorialRepository, never()).save(any(Tutorial.class));
        verify(authorRepository, never()).save(any(Author.class));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void missingAuthorOrCourseDoesNotAlterExistingTutorial() {
        Tutorial tutorial = tutorial(1L);
        Author originalAuthor = new Author("Ana");
        Course originalCourse = new Course("Java");
        tutorial.setAuthor(originalAuthor);
        tutorial.addCourse(originalCourse);
        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(tutorial));
        when(authorRepository.findById(2L)).thenReturn(Optional.empty());
        when(courseRepository.findById(3L)).thenReturn(Optional.empty());

        assertThat(relationshipService.setAuthor(1L, 2L)).isEmpty();
        assertThat(relationshipService.addCourse(1L, 3L)).isEmpty();
        assertThat(relationshipService.removeCourse(1L, 3L)).isEmpty();
        assertThat(tutorial.getAuthor()).isSameAs(originalAuthor);
        assertThat(tutorial.getCourses()).containsExactly(originalCourse);
        verify(tutorialRepository, never()).save(any(Tutorial.class));
    }

    private Tutorial tutorial(Long id) {
        Tutorial tutorial = new Tutorial("Spring", "REST API", true);
        ReflectionTestUtils.setField(tutorial, "id", id);
        return tutorial;
    }
}
