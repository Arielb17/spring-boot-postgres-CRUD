package com.bezkoder.spring_boot_jpa_postgresql.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.bezkoder.spring_boot_jpa_postgresql.dto.TutorialDto;
import com.bezkoder.spring_boot_jpa_postgresql.exception.ResourceNotFoundException;
import com.bezkoder.spring_boot_jpa_postgresql.service.TutorialService;

class TutorialControllerTest {

    private TutorialService tutorialService;
    private TutorialController tutorialController;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        tutorialService = mock(TutorialService.class);
        tutorialController = new TutorialController(tutorialService);
        pageable = PageRequest.of(1, 5);
    }

    @Test
    void getAllTutorialsReturnsOkWithTutorials() {
        Slice<TutorialDto> tutorials = filledSlice();
        when(tutorialService.getAllTutorials(null, pageable)).thenReturn(tutorials);
        ResponseEntity<Slice<TutorialDto>> response = tutorialController.getAllTutorials(null, pageable);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(tutorials);
        verify(tutorialService).getAllTutorials(null, pageable);
    }

    @Test
    void getAllTutorialsWithoutTitleReturnsOkWithEmptySlice() {
        Slice<TutorialDto> tutorials = emptySlice();
        when(tutorialService.getAllTutorials(null, pageable)).thenReturn(tutorials);

        ResponseEntity<Slice<TutorialDto>> response = tutorialController.getAllTutorials(null, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(tutorials);
        verify(tutorialService).getAllTutorials(null, pageable);
    }

    @Test
    void getAllTutorialsWithTitleReturnsOkWithTutorials() {
        Slice<TutorialDto> tutorials = filledSlice();
        when(tutorialService.getAllTutorials("Spring", pageable)).thenReturn(tutorials);

        ResponseEntity<Slice<TutorialDto>> response = tutorialController.getAllTutorials("Spring", pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(tutorials);
        verify(tutorialService).getAllTutorials("Spring", pageable);
    }

    @Test
    void getAllTutorialsReturnsOkWithEmptyListAndPreservesTitleFilter() {
        Slice<TutorialDto> tutorials = emptySlice();
        when(tutorialService.getAllTutorials("Spring", pageable)).thenReturn(tutorials);
        ResponseEntity<Slice<TutorialDto>> response = tutorialController.getAllTutorials("Spring", pageable);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(tutorials);
        verify(tutorialService).getAllTutorials("Spring", pageable);
    }

    @Test
    void getAllTutorialsPropagatesUnexpectedErrorToGlobalHandler() {
        when(tutorialService.getAllTutorials(null, pageable)).thenThrow(new RuntimeException());

        assertThatThrownBy(() -> tutorialController.getAllTutorials(null, pageable))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getAllTutorialsWithTitlePropagatesUnexpectedErrorToGlobalHandler() {
        when(tutorialService.getAllTutorials("Spring", pageable)).thenThrow(new RuntimeException());

        assertThatThrownBy(() -> tutorialController.getAllTutorials("Spring", pageable))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void findByExactTitleReturnsOkWithTutorialsAndForwardsTitle() {
        Slice<TutorialDto> tutorials = filledSlice();
        when(tutorialService.findByExactTitle("Spring", pageable)).thenReturn(tutorials);
        ResponseEntity<Slice<TutorialDto>> response = tutorialController.findByExactTitle("Spring", pageable);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(tutorials);
        verify(tutorialService).findByExactTitle("Spring", pageable);
    }

    @Test
    void findByExactTitleReturnsOkWithEmptyList() {
        Slice<TutorialDto> tutorials = emptySlice();
        when(tutorialService.findByExactTitle("Missing", pageable)).thenReturn(tutorials);
        ResponseEntity<Slice<TutorialDto>> response = tutorialController.findByExactTitle("Missing", pageable);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(tutorials);
    }

    @Test
    void findByExactTitlePropagatesUnexpectedErrorToGlobalHandler() {
        when(tutorialService.findByExactTitle("Spring", pageable)).thenThrow(new RuntimeException());

        assertThatThrownBy(() -> tutorialController.findByExactTitle("Spring", pageable))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getTutorialByIdReturnsOkWhenFound() {
        TutorialDto tutorial = new TutorialDto("Spring", "REST API", true);
        when(tutorialService.getTutorialById(1L)).thenReturn(tutorial);

        ResponseEntity<TutorialDto> response = tutorialController.getTutorialById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(tutorial);
    }

    @Test
    void getTutorialByIdPropagatesNotFoundToGlobalHandler() {
        when(tutorialService.getTutorialById(99L))
                .thenThrow(new ResourceNotFoundException("Tutorial não encontrado."));

        assertThatThrownBy(() -> tutorialController.getTutorialById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createTutorialReturnsCreated() {
        TutorialDto request = new TutorialDto("Spring", "REST API", false);
        TutorialDto created = new TutorialDto("Spring", "REST API", false);
        when(tutorialService.createTutorial(request)).thenReturn(created);

        ResponseEntity<TutorialDto> response = tutorialController.createTutorial(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(created);
    }

    @Test
    void createTutorialPropagatesUnexpectedErrorToGlobalHandler() {
        TutorialDto tutorial = new TutorialDto("Spring", "REST API", false);
        when(tutorialService.createTutorial(tutorial)).thenThrow(new RuntimeException());

        assertThatThrownBy(() -> tutorialController.createTutorial(tutorial))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void updateTutorialReturnsOkWhenFound() {
        TutorialDto request = new TutorialDto("Updated", "Updated description", true);
        TutorialDto updated = new TutorialDto("Updated", "Updated description", true);
        when(tutorialService.updateTutorial(1L, request)).thenReturn(updated);

        ResponseEntity<TutorialDto> response = tutorialController.updateTutorial(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(updated);
    }

    @Test
    void updateTutorialPropagatesNotFoundToGlobalHandler() {
        TutorialDto request = new TutorialDto("Updated", "Updated description", true);
        when(tutorialService.updateTutorial(99L, request))
                .thenThrow(new ResourceNotFoundException("Tutorial não encontrado."));

        assertThatThrownBy(() -> tutorialController.updateTutorial(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteTutorialReturnsNoContent() {
        ResponseEntity<Void> response = tutorialController.deleteTutorial(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(tutorialService).deleteTutorial(1L);
    }

    @Test
    void deleteTutorialPropagatesUnexpectedErrorToGlobalHandler() {
        doThrow(new RuntimeException()).when(tutorialService).deleteTutorial(1L);

        assertThatThrownBy(() -> tutorialController.deleteTutorial(1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void deleteAllTutorialsReturnsNoContent() {
        ResponseEntity<Void> response = tutorialController.deleteAllTutorials();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(tutorialService).deleteAllTutorials();
    }

    @Test
    void deleteAllTutorialsPropagatesUnexpectedErrorToGlobalHandler() {
        doThrow(new RuntimeException()).when(tutorialService).deleteAllTutorials();

        assertThatThrownBy(() -> tutorialController.deleteAllTutorials())
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void findByPublishedReturnsOkWithTutorials() {
        Slice<TutorialDto> tutorials = filledSlice();
        when(tutorialService.findByPublished(pageable)).thenReturn(tutorials);
        ResponseEntity<Slice<TutorialDto>> response = tutorialController.findByPublished(pageable);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(tutorials);
    }

    @Test
    void findByPublishedReturnsOkWithEmptyList() {
        Slice<TutorialDto> tutorials = emptySlice();
        when(tutorialService.findByPublished(pageable)).thenReturn(tutorials);
        ResponseEntity<Slice<TutorialDto>> response = tutorialController.findByPublished(pageable);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(tutorials);
    }

    @Test
    void findByPublishedPropagatesUnexpectedErrorToGlobalHandler() {
        when(tutorialService.findByPublished(pageable)).thenThrow(new RuntimeException());

        assertThatThrownBy(() -> tutorialController.findByPublished(pageable))
                .isInstanceOf(RuntimeException.class);
    }

    private Slice<TutorialDto> filledSlice() {
        return new SliceImpl<>(List.of(new TutorialDto("Spring", "REST API", true)), pageable, true);
    }

    private Slice<TutorialDto> emptySlice() {
        return new SliceImpl<>(List.of(), pageable, false);
    }
}
