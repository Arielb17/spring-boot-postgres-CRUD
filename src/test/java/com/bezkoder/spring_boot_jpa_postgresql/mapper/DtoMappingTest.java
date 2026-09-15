package com.bezkoder.spring_boot_jpa_postgresql.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.bezkoder.spring_boot_jpa_postgresql.dto.AuthorDto;
import com.bezkoder.spring_boot_jpa_postgresql.dto.CourseDto;
import com.bezkoder.spring_boot_jpa_postgresql.dto.TutorialDetailDto;
import com.bezkoder.spring_boot_jpa_postgresql.dto.TutorialDto;
import com.bezkoder.spring_boot_jpa_postgresql.model.Author;
import com.bezkoder.spring_boot_jpa_postgresql.model.Course;
import com.bezkoder.spring_boot_jpa_postgresql.model.Tutorial;
import com.bezkoder.spring_boot_jpa_postgresql.model.TutorialDetail;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class DtoMappingTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final CourseMapper courseMapper = new CourseMapperImpl();
    private final TutorialDetailMapper detailMapper = new TutorialDetailMapperImpl();
    private final TutorialMapper tutorialMapper = new TutorialMapperImpl(courseMapper, detailMapper);
    private final AuthorMapper authorMapper = new AuthorMapperImpl(tutorialMapper);

    @Test
    void mapsBidirectionalGraphAndPreservesExistingJsonForEveryResponseType() {
        Author author = new Author("Ana");
        Course course = new Course("Java");
        Tutorial tutorial = new Tutorial("Spring", "REST API", true);
        TutorialDetail detail = new TutorialDetail("DTO mapping", 20);
        ReflectionTestUtils.setField(author, "id", 1L);
        ReflectionTestUtils.setField(course, "id", 2L);
        ReflectionTestUtils.setField(tutorial, "id", 3L);
        ReflectionTestUtils.setField(detail, "id", 4L);
        author.addTutorial(tutorial);
        tutorial.addCourse(course);
        tutorial.setDetail(detail);

        assertSameJson(author, authorMapper.toDto(author));
        assertSameJson(course, courseMapper.toDto(course));
        assertSameJson(tutorial, tutorialMapper.toDto(tutorial));
        assertSameJson(detail, detailMapper.toDto(detail));

        assertSameJson(List.of(author), authorMapper.toDtoList(List.of(author)));
        assertSameJson(List.of(course), courseMapper.toDtoList(List.of(course)));
        assertSameJson(List.of(tutorial), tutorialMapper.toDtoList(List.of(tutorial)));
        assertSameJson(List.of(detail), detailMapper.toDtoList(List.of(detail)));
    }

    @Test
    void readsExistingJsonIntoDtosIncludingNestedRelationships() {
        AuthorDto author = jsonMapper.readValue("""
                {
                  "id": 1,
                  "name": "Ana",
                  "tutorials": [{
                    "id": 3,
                    "title": "Spring",
                    "description": "REST API",
                    "published": true,
                    "courses": [{"id": 2, "name": "Java"}],
                    "detail": {"id": 4, "content": "DTO mapping", "estimatedMinutes": 20}
                  }]
                }
                """, AuthorDto.class);

        TutorialDto tutorial = author.getTutorials().iterator().next();
        assertThat(tutorial.getAuthor()).isSameAs(author);
        assertThat(tutorial.getDetail().getTutorial()).isSameAs(tutorial);
        assertThat(tutorial.getCourses()).extracting(CourseDto::getName).containsExactly("Java");
        JsonNode serialized = jsonMapper.valueToTree(author);
        assertThat(serialized.get("tutorials").get(0).has("author")).isFalse();
        assertThat(serialized.get("tutorials").get(0).get("detail").has("tutorial")).isFalse();
        assertThat(serialized.get("tutorials").get(0).get("courses").get(0).has("tutorials")).isFalse();

        TutorialDto tutorialRequest = jsonMapper.readValue(
                """
                {"title":"Spring","description":"REST API","published":true}
                """, TutorialDto.class);
        assertThat(tutorialRequest.getTitle()).isEqualTo("Spring");
        assertThat(tutorialRequest.isPublished()).isTrue();
        assertThat(tutorialRequest.getCourses()).isEmpty();

        CourseDto courseRequest = jsonMapper.readValue("""
                {"name":"Java"}
                """, CourseDto.class);
        assertThat(courseRequest.getName()).isEqualTo("Java");

        TutorialDetailDto detailRequest = jsonMapper.readValue(
                """
                {"content":"DTO mapping","estimatedMinutes":20}
                """, TutorialDetailDto.class);
        assertThat(detailRequest.getContent()).isEqualTo("DTO mapping");
        assertThat(detailRequest.getEstimatedMinutes()).isEqualTo(20);
    }

    private void assertSameJson(Object entity, Object dto) {
        JsonNode entityJson = jsonMapper.valueToTree(entity);
        JsonNode dtoJson = jsonMapper.valueToTree(dto);
        assertThat(dtoJson).isEqualTo(entityJson);
    }
}
