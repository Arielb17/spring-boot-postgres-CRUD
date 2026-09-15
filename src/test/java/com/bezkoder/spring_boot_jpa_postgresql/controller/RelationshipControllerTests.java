package com.bezkoder.spring_boot_jpa_postgresql.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bezkoder.spring_boot_jpa_postgresql.dto.AuthorDto;
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
    void shouldPreserveRelationshipSuccessAndNotFoundContracts() throws Exception {
        when(service.createAuthor(any(AuthorDto.class))).thenReturn(new AuthorDto("Ada"));
        when(service.getAuthor(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/authors")
                        .contentType("application/json")
                        .content("{\"name\":\"Ada\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Ada"));

        mockMvc.perform(get("/api/authors/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    void shouldApplyGlobalConflictContractToRelationshipController() throws Exception {
        when(service.createAuthor(any(AuthorDto.class)))
                .thenThrow(new DataIntegrityViolationException("secret SQL details"));

        mockMvc.perform(post("/api/authors")
                        .contentType("application/json")
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
}
