package com.bezkoder.spring_boot_jpa_postgresql.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.bezkoder.spring_boot_jpa_postgresql.model.Tutorial;
import com.bezkoder.spring_boot_jpa_postgresql.repository.TutorialRepository;

class TutorialServiceImplTest {

    private TutorialRepository tutorialRepository;
    private TutorialServiceImpl tutorialService;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        tutorialRepository = mock(TutorialRepository.class);
        tutorialService = new TutorialServiceImpl(tutorialRepository);
        pageable = PageRequest.of(2, 7);
    }

    @Test
    void getAllTutorialsWithoutTitleReturnsFilledSlice() {
        Slice<Tutorial> expected = filledSlice(true);
        when(tutorialRepository.findAllBy(pageable)).thenReturn(expected);
        assertThat(tutorialService.getAllTutorials(null, pageable)).isSameAs(expected);
        verify(tutorialRepository).findAllBy(pageable);
        verifyNoMoreInteractions(tutorialRepository);
    }

    @Test
    void getAllTutorialsWithoutTitleReturnsEmptySlice() {
        Slice<Tutorial> expected = emptySlice();
        when(tutorialRepository.findAllBy(pageable)).thenReturn(expected);
        assertThat(tutorialService.getAllTutorials(null, pageable)).isSameAs(expected);
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
        assertThat(tutorialService.getAllTutorials("Spring", pageable)).isSameAs(expected);
        assertThat(tutorialService.getAllTutorials("", pageable)).isSameAs(empty);
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
        assertThat(tutorialService.findByExactTitle("Spring", pageable)).isSameAs(expected);
        assertThat(tutorialService.findByExactTitle("Missing", pageable)).isSameAs(empty);
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
        assertThat(tutorialService.findByPublished(pageable)).isSameAs(expected);
        assertThat(tutorialService.findByPublished(pageable)).isSameAs(empty);
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
        Tutorial request = new Tutorial("Spring", "REST API", true);
        when(tutorialRepository.save(any(Tutorial.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Tutorial result = tutorialService.createTutorial(request);
        assertThat(result.isPublished()).isFalse();
        verify(tutorialRepository).save(argThat(tutorial ->
                "Spring".equals(tutorial.getTitle()) && !tutorial.isPublished()));
    }

    private Slice<Tutorial> filledSlice(boolean hasNext) {
        return new SliceImpl<>(List.of(new Tutorial("Spring", "REST API", true)), pageable, hasNext);
    }

    private Slice<Tutorial> emptySlice() {
        return new SliceImpl<>(List.of(), pageable, false);
    }
}
