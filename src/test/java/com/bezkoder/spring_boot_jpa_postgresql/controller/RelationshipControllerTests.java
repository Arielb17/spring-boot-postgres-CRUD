package com.bezkoder.spring_boot_jpa_postgresql.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bezkoder.spring_boot_jpa_postgresql.dto.AuthorDto;
import com.bezkoder.spring_boot_jpa_postgresql.dto.TutorialDetailDto;
import com.bezkoder.spring_boot_jpa_postgresql.exception.ResourceNotFoundException;
import com.bezkoder.spring_boot_jpa_postgresql.service.RelationshipService;

@WebMvcTest(RelationshipController.class)
class RelationshipControllerTests {

    private static final String CONFLICT_DETAIL = "A operação entra em conflito com os dados existentes.";
    private static final String INTERNAL_ERROR_DETAIL = "Ocorreu um erro interno ao processar a requisição.";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RelationshipService service;

    @Test
    void shouldCreateAuthor() throws Exception {
        when(service.createAuthor(any(AuthorDto.class))).thenReturn(new AuthorDto("Ada"));

        mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ada\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Ada"));
    }

    @Test
    void shouldReturnNotFoundWhenAuthorDoesNotExist() throws Exception {
        assertNotFound(get("/api/authors/99"), "Autor não encontrado.",
                () -> when(service.getAuthor(99L))
                        .thenThrow(new ResourceNotFoundException("Autor não encontrado.")));
    }

    @Test
    void shouldReturnNotFoundWhenCourseDoesNotExist() throws Exception {
        assertNotFound(get("/api/courses/99"), "Curso não encontrado.",
                () -> when(service.getCourse(99L))
                        .thenThrow(new ResourceNotFoundException("Curso não encontrado.")));
    }

    @Test
    void shouldReturnNotFoundWhenSettingMissingAuthor() throws Exception {
        assertNotFound(post("/api/tutorials/1/author/99"), "Autor não encontrado.",
                () -> when(service.setAuthor(1L, 99L))
                        .thenThrow(new ResourceNotFoundException("Autor não encontrado.")));
    }

    @Test
    void shouldReturnNotFoundWhenAddingMissingCourse() throws Exception {
        assertNotFound(post("/api/tutorials/1/courses/99"), "Curso não encontrado.",
                () -> when(service.addCourse(1L, 99L))
                        .thenThrow(new ResourceNotFoundException("Curso não encontrado.")));
    }

    @Test
    void shouldReturnNotFoundWhenCourseIsNotAssociated() throws Exception {
        assertNotFound(delete("/api/tutorials/1/courses/2"), "Curso não está associado ao tutorial.",
                () -> doThrow(new ResourceNotFoundException("Curso não está associado ao tutorial."))
                        .when(service).removeCourse(1L, 2L));
    }

    @Test
    void shouldReturnNotFoundWhenCreatingDetailForMissingTutorial() throws Exception {
        when(service.createDetail(org.mockito.ArgumentMatchers.eq(99L), any(TutorialDetailDto.class)))
                .thenThrow(new ResourceNotFoundException("Tutorial não encontrado."));

        mockMvc.perform(post("/api/tutorials/99/detail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Content\",\"estimatedMinutes\":20}"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Tutorial não encontrado."));
    }

    @Test
    void shouldReturnNotFoundWhenDetailDoesNotExist() throws Exception {
        assertNotFound(get("/api/tutorials/1/detail"), "Detalhe do tutorial não encontrado.",
                () -> when(service.getDetail(1L))
                        .thenThrow(new ResourceNotFoundException("Detalhe do tutorial não encontrado.")));
    }

    @Test
    void shouldApplyGlobalConflictContractToRelationshipController() throws Exception {
        when(service.createAuthor(any(AuthorDto.class)))
                .thenThrow(new DataIntegrityViolationException("secret SQL details"));

        mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"duplicate\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(CONFLICT_DETAIL))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("secret SQL details"))));
    }

    @Test
    void shouldApplyGlobalUnexpectedErrorContractToRelationshipController() throws Exception {
        when(service.createAuthor(any(AuthorDto.class))).thenThrow(new RuntimeException("service failure"));

        mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ada\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(INTERNAL_ERROR_DETAIL))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("service failure"))));
    }

    private void assertNotFound(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,
            String expectedDetail,
            Runnable stub) throws Exception {
        stub.run();
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(expectedDetail));
    }
}
