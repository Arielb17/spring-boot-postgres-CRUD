package com.bezkoder.spring_boot_jpa_postgresql.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import com.bezkoder.spring_boot_jpa_postgresql.dto.AuthorDto;
import com.bezkoder.spring_boot_jpa_postgresql.dto.CourseDto;
import com.bezkoder.spring_boot_jpa_postgresql.dto.TutorialDetailDto;
import com.bezkoder.spring_boot_jpa_postgresql.dto.TutorialDto;
import com.bezkoder.spring_boot_jpa_postgresql.mapper.CourseMapperImpl;
import com.bezkoder.spring_boot_jpa_postgresql.mapper.TutorialDetailMapperImpl;
import com.bezkoder.spring_boot_jpa_postgresql.mapper.TutorialMapperImpl;
import com.bezkoder.spring_boot_jpa_postgresql.model.Author;
import com.bezkoder.spring_boot_jpa_postgresql.model.Course;
import com.bezkoder.spring_boot_jpa_postgresql.model.Tutorial;
import com.bezkoder.spring_boot_jpa_postgresql.model.TutorialDetail;
import com.bezkoder.spring_boot_jpa_postgresql.repository.TutorialRepository;

class TutorialServiceImplTest {

    private TutorialRepository tutorialRepository;
    private TutorialServiceImpl tutorialService;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        tutorialRepository = mock(TutorialRepository.class);
        tutorialService = new TutorialServiceImpl(tutorialRepository,
                new TutorialMapperImpl(new CourseMapperImpl(), new TutorialDetailMapperImpl()));
        pageable = PageRequest.of(2, 7);
    }

    @Test
    void getAllTutorialsWithoutTitleReturnsFilledSlice() {
        Slice<Tutorial> expected = filledSlice(true);
        when(tutorialRepository.findAllBy(pageable)).thenReturn(expected);
        assertMappedSlice(tutorialService.getAllTutorials(null, pageable), expected);
        verify(tutorialRepository).findAllBy(pageable);
        verifyNoMoreInteractions(tutorialRepository);
    }

    @Test
    void getAllTutorialsWithoutTitleReturnsEmptySlice() {
        Slice<Tutorial> expected = emptySlice();
        when(tutorialRepository.findAllBy(pageable)).thenReturn(expected);
        assertMappedSlice(tutorialService.getAllTutorials(null, pageable), expected);
        verify(tutorialRepository).findAllBy(pageable);
        verifyNoMoreInteractions(tutorialRepository);
    }

    @Test
    void getAllTutorialsWithoutTitlePropagatesException() {
        RuntimeException failure = new RuntimeException("find all failed");
        when(tutorialRepository.findAllBy(pageable)).thenThrow(failure);
        assertThatThrownBy(() -> tutorialService.getAllTutorials(null, pageable)).isSameAs(failure);
        verify(tutorialRepository).findAllBy(pageable);
        verifyNoMoreInteractions(tutorialRepository);
    }

    @Test
    void getAllTutorialsWithTitleReturnsFilledAndEmptySlices() {
        Slice<Tutorial> expected = filledSlice(true);
        Slice<Tutorial> empty = emptySlice();
        when(tutorialRepository.findByTitleContaining("Spring", pageable)).thenReturn(expected);
        when(tutorialRepository.findByTitleContaining("", pageable)).thenReturn(empty);
        assertMappedSlice(tutorialService.getAllTutorials("Spring", pageable), expected);
        assertMappedSlice(tutorialService.getAllTutorials("", pageable), empty);
        verify(tutorialRepository).findByTitleContaining("Spring", pageable);
        verify(tutorialRepository).findByTitleContaining("", pageable);
        verifyNoMoreInteractions(tutorialRepository);
    }

    @Test
    void getAllTutorialsWithTitlePropagatesException() {
        RuntimeException failure = new RuntimeException("contains failed");
        when(tutorialRepository.findByTitleContaining("Spring", pageable)).thenThrow(failure);
        assertThatThrownBy(() -> tutorialService.getAllTutorials("Spring", pageable)).isSameAs(failure);
        verify(tutorialRepository).findByTitleContaining("Spring", pageable);
        verifyNoMoreInteractions(tutorialRepository);
    }

    @Test
    void findByExactTitleReturnsFilledAndEmptySlices() {
        Slice<Tutorial> expected = filledSlice(false);
        Slice<Tutorial> empty = emptySlice();
        when(tutorialRepository.findByTitle("Spring", pageable)).thenReturn(expected);
        when(tutorialRepository.findByTitle("Missing", pageable)).thenReturn(empty);
        assertMappedSlice(tutorialService.findByExactTitle("Spring", pageable), expected);
        assertMappedSlice(tutorialService.findByExactTitle("Missing", pageable), empty);
        verify(tutorialRepository).findByTitle("Spring", pageable);
        verify(tutorialRepository).findByTitle("Missing", pageable);
        verifyNoMoreInteractions(tutorialRepository);
    }

    @Test
    void findByExactTitlePropagatesException() {
        RuntimeException failure = new RuntimeException("exact failed");
        when(tutorialRepository.findByTitle("Spring", pageable)).thenThrow(failure);
        assertThatThrownBy(() -> tutorialService.findByExactTitle("Spring", pageable)).isSameAs(failure);
        verify(tutorialRepository).findByTitle("Spring", pageable);
        verifyNoMoreInteractions(tutorialRepository);
    }

    @Test
    void findByPublishedReturnsFilledAndEmptySlices() {
        Slice<Tutorial> expected = filledSlice(true);
        Slice<Tutorial> empty = emptySlice();
        when(tutorialRepository.findByPublished(true, pageable)).thenReturn(expected, empty);
        assertMappedSlice(tutorialService.findByPublished(pageable), expected);
        assertMappedSlice(tutorialService.findByPublished(pageable), empty);
        verify(tutorialRepository, times(2)).findByPublished(true, pageable);
        verifyNoMoreInteractions(tutorialRepository);
    }

    @Test
    void findByPublishedPropagatesException() {
        RuntimeException failure = new RuntimeException("published failed");
        when(tutorialRepository.findByPublished(true, pageable)).thenThrow(failure);
        assertThatThrownBy(() -> tutorialService.findByPublished(pageable)).isSameAs(failure);
        verify(tutorialRepository).findByPublished(true, pageable);
        verifyNoMoreInteractions(tutorialRepository);
    }

    @Test
    void createTutorialAlwaysUnpublished() {
        TutorialDto request = new TutorialDto("Spring", "REST API", true);
        when(tutorialRepository.save(any(Tutorial.class))).thenAnswer(invocation -> invocation.getArgument(0));
        TutorialDto result = tutorialService.createTutorial(request);
        assertThat(result.getTitle()).isEqualTo("Spring");
        assertThat(result.getDescription()).isEqualTo("REST API");
        assertThat(result.isPublished()).isFalse();
        verify(tutorialRepository).save(argThat(tutorial ->
                "Spring".equals(tutorial.getTitle()) && !tutorial.isPublished()));
    }

    @Test
    void createTutorialIgnoresSubmittedIdAndRelationships() {
        TutorialDto request = new TutorialDto("Spring", "REST API", true);
        request.setId(999L);
        request.setAuthor(new AuthorDto());
        request.setCourses(Set.of(new CourseDto()));
        request.setDetail(new TutorialDetailDto());
        when(tutorialRepository.save(any(Tutorial.class))).thenAnswer(invocation -> {
            Tutorial saved = invocation.getArgument(0);
            assertThat(saved.getId()).isNull();
            assertThat(saved.getAuthor()).isNull();
            assertThat(saved.getCourses()).isEmpty();
            assertThat(saved.getDetail()).isNull();
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });

        TutorialDto result = tutorialService.createTutorial(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAuthor()).isNull();
        assertThat(result.getCourses()).isEmpty();
        assertThat(result.getDetail()).isNull();
    }

    @Test
    void updateTutorialPreservesIdAndRelationshipsOfExistingEntity() {
        Tutorial existing = new Tutorial("Original", "Original description", false);
        ReflectionTestUtils.setField(existing, "id", 1L);
        Author author = new Author("Original author");
        Course course = new Course("Original course");
        TutorialDetail detail = new TutorialDetail("Original content", 15);
        existing.setAuthor(author);
        existing.addCourse(course);
        existing.setDetail(detail);

        TutorialDto request = new TutorialDto("Updated", "Updated description", true);
        request.setId(999L);
        request.setAuthor(new AuthorDto());
        request.setCourses(Set.of(new CourseDto()));
        request.setDetail(new TutorialDetailDto());
        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(tutorialRepository.save(existing)).thenReturn(existing);

        Optional<TutorialDto> result = tutorialService.updateTutorial(1L, request);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getTitle()).isEqualTo("Updated");
        assertThat(result.get().getDescription()).isEqualTo("Updated description");
        assertThat(result.get().isPublished()).isTrue();
        assertThat(existing.getId()).isEqualTo(1L);
        assertThat(existing.getAuthor()).isSameAs(author);
        assertThat(existing.getCourses()).containsExactly(course);
        assertThat(existing.getDetail()).isSameAs(detail);
        assertThat(author.getTutorials()).containsExactly(existing);
        assertThat(course.getTutorials()).containsExactly(existing);
        assertThat(detail.getTutorial()).isSameAs(existing);
        verify(tutorialRepository).findById(1L);
        verify(tutorialRepository).save(existing);
        verifyNoMoreInteractions(tutorialRepository);
    }

    private void assertMappedSlice(Slice<TutorialDto> actual, Slice<Tutorial> expected) {
        assertThat(actual.getPageable()).isEqualTo(expected.getPageable());
        assertThat(actual.hasNext()).isEqualTo(expected.hasNext());
        assertThat(actual.getContent())
                .extracting(TutorialDto::getId, TutorialDto::getTitle,
                        TutorialDto::getDescription, TutorialDto::isPublished)
                .containsExactlyElementsOf(expected.getContent().stream()
                        .map(tutorial -> tuple(tutorial.getId(), tutorial.getTitle(),
                                tutorial.getDescription(), tutorial.isPublished()))
                        .toList());
    }

    private Slice<Tutorial> filledSlice(boolean hasNext) {
        return new SliceImpl<>(List.of(new Tutorial("Spring", "REST API", true)), pageable, hasNext);
    }

    private Slice<Tutorial> emptySlice() {
        return new SliceImpl<>(List.of(), pageable, false);
    }
}
