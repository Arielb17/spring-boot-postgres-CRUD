package com.bezkoder.spring_boot_jpa_postgresql.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bezkoder.spring_boot_jpa_postgresql.dto.TutorialDto;
import com.bezkoder.spring_boot_jpa_postgresql.service.TutorialService;

@WebMvcTest(TutorialController.class)
class TutorialControllerTests {

    private static final String INVALID_JSON_DETAIL = "O corpo da requisição contém JSON inválido.";
    private static final String MISSING_PARAMETER_DETAIL = "Parâmetro obrigatório ausente.";
    private static final String INVALID_PARAMETER_DETAIL = "Parâmetro da requisição inválido.";
    private static final String CONFLICT_DETAIL = "A operação entra em conflito com os dados existentes.";
    private static final String INTERNAL_ERROR_DETAIL = "Ocorreu um erro interno ao processar a requisição.";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TutorialService service;

    @ParameterizedTest(name = "{0} accepts {1} pagination")
    @MethodSource("paginationCases")
    void shouldApplyPaginationToEverySliceRoute(
            String routeName,
            String path,
            String title,
            int page,
            int requestedSize,
            int expectedSize,
            String sortProperty,
            boolean descending,
            boolean hasNext) throws Exception {
        stubRoute(title, routeName, hasNext);

        var request = get(path);
        if (title != null) {
            request.param("title", title);
        }
        if (page != 0) {
            request.param("page", String.valueOf(page));
        }
        if (requestedSize != 20) {
            request.param("size", String.valueOf(requestedSize));
        }
        if (!sortProperty.equals("id") || descending) {
            request.param("sort", sortProperty + "," + (descending ? "desc" : "asc"));
        }

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Spring Boot"))
                .andExpect(jsonPath("$.number").value(page))
                .andExpect(jsonPath("$.size").value(expectedSize))
                .andExpect(jsonPath("$.numberOfElements").value(1))
                .andExpect(jsonPath("$.first").value(page == 0))
                .andExpect(jsonPath("$.last").value(!hasNext))
                .andExpect(jsonPath("$.empty").value(false))
                .andExpect(jsonPath("$.totalElements").doesNotExist())
                .andExpect(jsonPath("$.totalPages").doesNotExist());

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verifyRoute(routeName, title, pageable);
        assertThat(pageable.getValue().getPageNumber()).isEqualTo(page);
        assertThat(pageable.getValue().getPageSize()).isEqualTo(expectedSize);
        Sort.Direction direction = descending ? Sort.Direction.DESC : Sort.Direction.ASC;
        assertThat(pageable.getValue().getSort()).isEqualTo(Sort.by(direction, sortProperty));
    }

    private static Stream<Arguments> paginationCases() {
        return Stream.of(
                Arguments.of("all", "/api/tutorials", null, 0, 20, 20, "id", false, true),
                Arguments.of("all", "/api/tutorials", null, 2, 7, 7, "title", true, false),
                Arguments.of("all", "/api/tutorials", null, 0, 101, 100, "id", false, false),
                Arguments.of("all", "/api/tutorials", null, 1, 8, 8, "id", false, false),
                Arguments.of("all-title", "/api/tutorials", "Spring", 0, 20, 20, "id", false, true),
                Arguments.of("all-title", "/api/tutorials", "Spring", 2, 7, 7, "title", true, false),
                Arguments.of("all-title", "/api/tutorials", "Spring", 0, 101, 100, "id", false, false),
                Arguments.of("all-title", "/api/tutorials", "Spring", 1, 8, 8, "id", false, false),
                Arguments.of("title", "/api/tutorials/by-title", "Spring", 0, 20, 20, "id", false, true),
                Arguments.of("title", "/api/tutorials/by-title", "Spring", 2, 7, 7, "title", true, false),
                Arguments.of("title", "/api/tutorials/by-title", "Spring", 0, 101, 100, "id", false, false),
                Arguments.of("title", "/api/tutorials/by-title", "Spring", 1, 8, 8, "id", false, false),
                Arguments.of("published", "/api/tutorials/published", null, 0, 20, 20, "id", false, true),
                Arguments.of("published", "/api/tutorials/published", null, 2, 7, 7, "title", true, false),
                Arguments.of("published", "/api/tutorials/published", null, 0, 101, 100, "id", false, false),
                Arguments.of("published", "/api/tutorials/published", null, 1, 8, 8, "id", false, false));
    }

    @ParameterizedTest(name = "{0} returns an empty slice")
    @MethodSource("emptySliceRoutes")
    void shouldReturnEmptySlice(String routeName, String path, String title) throws Exception {
        stubEmptyRoute(title, routeName);

        var request = get(path);
        if (title != null) {
            request.param("title", title);
        }

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.numberOfElements").value(0))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.empty").value(true))
                .andExpect(jsonPath("$.totalElements").doesNotExist())
                .andExpect(jsonPath("$.totalPages").doesNotExist());
    }

    private static Stream<Arguments> emptySliceRoutes() {
        return Stream.of(
                Arguments.of("all", "/api/tutorials", null),
                Arguments.of("all-title", "/api/tutorials", "missing"),
                Arguments.of("title", "/api/tutorials/by-title", "missing"),
                Arguments.of("published", "/api/tutorials/published", null));
    }

    @ParameterizedTest(name = "{0} returns a generic 500 ProblemDetail")
    @MethodSource("errorRoutes")
    void shouldReturnInternalServerErrorWithGenericProblemDetail(String routeName, String path, String title) throws Exception {
        stubErrorRoute(title, routeName);

        var request = get(path);
        if (title != null) {
            request.param("title", title);
        }

        mockMvc.perform(request)
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(INTERNAL_ERROR_DETAIL))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("service failure"))));
    }

    private static Stream<Arguments> errorRoutes() {
        return Stream.of(
                Arguments.of("all", "/api/tutorials", null),
                Arguments.of("all-title", "/api/tutorials", "bad"),
                Arguments.of("title", "/api/tutorials/by-title", "bad"),
                Arguments.of("published", "/api/tutorials/published", null));
    }

    @Test
    void shouldRejectByTitleRequestWithoutTitle() throws Exception {
        mockMvc.perform(get("/api/tutorials/by-title"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(MISSING_PARAMETER_DETAIL));
    }

    @Test
    void shouldRejectMalformedJson() throws Exception {
        mockMvc.perform(post("/api/tutorials")
                .contentType(MediaType.APPLICATION_JSON)
                        .content("{malformed"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(INVALID_JSON_DETAIL));
    }

    @Test
    void shouldRejectInvalidTutorialId() throws Exception {
        mockMvc.perform(get("/api/tutorials/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(INVALID_PARAMETER_DETAIL));
    }

    @Test
    void shouldReturnConflictForDataIntegrityViolation() throws Exception {
        when(service.createTutorial(any(TutorialDto.class)))
                .thenThrow(new DataIntegrityViolationException("secret SQL details"));

        mockMvc.perform(post("/api/tutorials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"duplicate\",\"description\":\"x\",\"published\":false}"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(CONFLICT_DETAIL))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secret SQL details"))));
    }

    @Test
    void shouldPreserveAllowHeaderForMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/api/tutorials/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", org.hamcrest.Matchers.containsString("GET")));
    }

    @Test
    void shouldPreserveAcceptHeaderForUnsupportedMediaType() throws Exception {
        mockMvc.perform(post("/api/tutorials")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("plain text"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(header().string("Accept", org.hamcrest.Matchers.containsString("application/json")));
    }

    @Test
    void shouldReturnNotFoundWhenTutorialIsAbsent() throws Exception {
        when(service.getTutorialById(99L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/tutorials/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    void shouldReturnAllTutorials() throws Exception {
        when(service.getAllTutorials(isNull(), any(Pageable.class)))
                .thenReturn(new SliceImpl<>(
                        List.of(new TutorialDto("Spring Boot", "Tutorial description", false)),
                        PageRequest.of(0, 20, Sort.by("id").ascending()),
                        false));

        mockMvc.perform(get("/api/tutorials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Spring Boot"));
    }

    @Test
    void shouldFilterTutorialsByTitle() throws Exception {
        when(service.getAllTutorials(eq("PostgreSQL"), any(Pageable.class)))
                .thenReturn(new SliceImpl<>(
                        List.of(new TutorialDto("PostgreSQL", "Tutorial description", false)),
                        PageRequest.of(1, 7, Sort.by("title").descending()),
                        false));

        mockMvc.perform(get("/api/tutorials")
                        .param("title", "PostgreSQL")
                        .param("page", "1")
                        .param("size", "7")
                        .param("sort", "title,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("PostgreSQL"));

        verify(service).getAllTutorials(eq("PostgreSQL"), any(Pageable.class));
    }

    @Test
    void shouldReturnTutorialById() throws Exception {
        when(service.getTutorialById(1L))
                .thenReturn(Optional.of(new TutorialDto("Spring Data JPA", "Tutorial description", false)));

        mockMvc.perform(get("/api/tutorials/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Spring Data JPA"));
    }

    @Test
    void shouldCreateTutorialAsUnpublished() throws Exception {
        when(service.createTutorial(any(TutorialDto.class)))
                .thenReturn(new TutorialDto("Maven", "Tutorial description", false));

        mockMvc.perform(post("/api/tutorials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Maven",
                                  "description": "Tutorial description",
                                  "published": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Maven"))
                .andExpect(jsonPath("$.published").value(false));
    }

    @Test
    void shouldReturnGenericProblemDetailWhenCreateFailsUnexpectedly() throws Exception {
        when(service.createTutorial(any(TutorialDto.class))).thenThrow(new RuntimeException("service failure"));

        mockMvc.perform(post("/api/tutorials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Maven\",\"description\":\"x\",\"published\":false}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(INTERNAL_ERROR_DETAIL))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("service failure"))));
    }

    @Test
    void shouldUpdateTutorial() throws Exception {
        when(service.updateTutorial(eq(1L), any(TutorialDto.class)))
                .thenReturn(Optional.of(new TutorialDto("New title", "New description", true)));

        mockMvc.perform(put("/api/tutorials/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "New title",
                                  "description": "New description",
                                  "published": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New title"))
                .andExpect(jsonPath("$.published").value(true));
    }

    @Test
    void shouldReturnGenericProblemDetailWhenUpdateFailsUnexpectedly() throws Exception {
        when(service.updateTutorial(eq(1L), any(TutorialDto.class)))
                .thenThrow(new RuntimeException("service failure"));

        mockMvc.perform(put("/api/tutorials/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New title\",\"description\":\"x\",\"published\":true}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(INTERNAL_ERROR_DETAIL))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("service failure"))));
    }

    @Test
    void shouldDeleteTutorialById() throws Exception {
        mockMvc.perform(delete("/api/tutorials/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(service).deleteTutorial(1L);
    }

    @Test
    void shouldReturnGenericProblemDetailWhenDeleteTutorialFailsUnexpectedly() throws Exception {
        doThrow(new RuntimeException("service failure")).when(service).deleteTutorial(1L);

        mockMvc.perform(delete("/api/tutorials/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(INTERNAL_ERROR_DETAIL))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("service failure"))));
    }

    @Test
    void shouldDeleteAllTutorials() throws Exception {
        mockMvc.perform(delete("/api/tutorials"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(service).deleteAllTutorials();
    }

    @Test
    void shouldReturnGenericProblemDetailWhenDeleteAllTutorialsFailsUnexpectedly() throws Exception {
        doThrow(new RuntimeException("service failure")).when(service).deleteAllTutorials();

        mockMvc.perform(delete("/api/tutorials"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(INTERNAL_ERROR_DETAIL))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("service failure"))));
    }

    @Test
    void shouldReturnPublishedTutorials() throws Exception {
        when(service.findByPublished(any(Pageable.class)))
                .thenReturn(new SliceImpl<>(
                        List.of(new TutorialDto("Published", "Tutorial description", true)),
                        PageRequest.of(0, 20, Sort.by("id").ascending()),
                        false));

        mockMvc.perform(get("/api/tutorials/published"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].published").value(true));

        verify(service).findByPublished(any(Pageable.class));
    }

    private void stubRoute(String title, String routeName, boolean hasNext) {
        if (routeName.equals("all") || routeName.equals("all-title")) {
            when(service.getAllTutorials(
                    routeName.equals("all") ? isNull() : eq(title), any(Pageable.class)))
                    .thenAnswer(invocation -> sliceFor(invocation.getArgument(1), routeName, hasNext));
        } else if (routeName.equals("title")) {
            when(service.findByExactTitle(eq(title), any(Pageable.class)))
                    .thenAnswer(invocation -> sliceFor(invocation.getArgument(1), routeName, hasNext));
        } else {
            when(service.findByPublished(any(Pageable.class)))
                    .thenAnswer(invocation -> sliceFor(invocation.getArgument(0), routeName, hasNext));
        }
    }

    private Slice<TutorialDto> sliceFor(Pageable pageable, String routeName, boolean hasNext) {
        return new SliceImpl<>(
                List.of(new TutorialDto("Spring Boot", "Tutorial description", routeName.equals("published"))),
                pageable,
                hasNext);
    }

    private void stubEmptyRoute(String title, String routeName) {
        if (routeName.equals("all") || routeName.equals("all-title")) {
            when(service.getAllTutorials(
                    routeName.equals("all") ? isNull() : eq(title), any(Pageable.class)))
                    .thenAnswer(invocation -> emptySlice(invocation.getArgument(1)));
        } else if (routeName.equals("title")) {
            when(service.findByExactTitle(eq(title), any(Pageable.class)))
                    .thenAnswer(invocation -> emptySlice(invocation.getArgument(1)));
        } else {
            when(service.findByPublished(any(Pageable.class)))
                    .thenAnswer(invocation -> emptySlice(invocation.getArgument(0)));
        }
    }

    private Slice<TutorialDto> emptySlice(Pageable pageable) {
        return new SliceImpl<>(List.of(), pageable, false);
    }

    private void stubErrorRoute(String title, String routeName) {
        if (routeName.equals("all") || routeName.equals("all-title")) {
            when(service.getAllTutorials(
                    routeName.equals("all") ? isNull() : eq(title), any(Pageable.class)))
                    .thenThrow(new RuntimeException("service failure"));
        } else if (routeName.equals("title")) {
            when(service.findByExactTitle(eq(title), any(Pageable.class)))
                    .thenThrow(new RuntimeException("service failure"));
        } else {
            when(service.findByPublished(any(Pageable.class)))
                    .thenThrow(new RuntimeException("service failure"));
        }
    }

    private void verifyRoute(String routeName, String title, ArgumentCaptor<Pageable> pageable) {
        if (routeName.equals("all") || routeName.equals("all-title")) {
            if (routeName.equals("all")) {
                verify(service).getAllTutorials(isNull(), pageable.capture());
            } else {
                verify(service).getAllTutorials(eq(title), pageable.capture());
            }
        } else if (routeName.equals("title")) {
            verify(service).findByExactTitle(eq(title), pageable.capture());
        } else {
            verify(service).findByPublished(pageable.capture());
        }
    }
}
