package com.bezkoder.spring_boot_jpa_postgresql.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bezkoder.spring_boot_jpa_postgresql.dto.AuthorDto;
import com.bezkoder.spring_boot_jpa_postgresql.dto.CourseDto;
import com.bezkoder.spring_boot_jpa_postgresql.dto.TutorialDto;
import com.bezkoder.spring_boot_jpa_postgresql.dto.TutorialDetailDto;
import com.bezkoder.spring_boot_jpa_postgresql.service.RelationshipService;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api")
public class RelationshipController {
    private final RelationshipService relationshipService;

    public RelationshipController(RelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }

    @PostMapping("/authors")
    public ResponseEntity<AuthorDto> createAuthor(@RequestBody AuthorDto authorDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(relationshipService.createAuthor(authorDto));
    }

    @GetMapping("/authors/{id}")
    public ResponseEntity<AuthorDto> getAuthor(@PathVariable Long id) {
        return relationshipService.getAuthor(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/tutorials/{tutorialId}/author/{authorId}")
    public ResponseEntity<TutorialDto> setAuthor(@PathVariable Long tutorialId, @PathVariable Long authorId) {
        return relationshipService.setAuthor(tutorialId, authorId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/courses")
    public ResponseEntity<CourseDto> createCourse(@RequestBody CourseDto courseDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(relationshipService.createCourse(courseDto));
    }

    @GetMapping("/courses/{id}")
    public ResponseEntity<CourseDto> getCourse(@PathVariable Long id) {
        return relationshipService.getCourse(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/tutorials/{tutorialId}/courses/{courseId}")
    public ResponseEntity<TutorialDto> addCourse(@PathVariable Long tutorialId, @PathVariable Long courseId) {
        return relationshipService.addCourse(tutorialId, courseId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/tutorials/{tutorialId}/courses/{courseId}")
    public ResponseEntity<Void> removeCourse(@PathVariable Long tutorialId, @PathVariable Long courseId) {
        return relationshipService.removeCourse(tutorialId, courseId).map(ignored -> ResponseEntity.noContent().<Void>build())
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/tutorials/{tutorialId}/detail")
    public ResponseEntity<TutorialDetailDto> createDetail(@PathVariable Long tutorialId,
            @RequestBody TutorialDetailDto detailDto) {
        return relationshipService.createDetail(tutorialId, detailDto)
                .map(detail -> ResponseEntity.status(HttpStatus.CREATED).body(detail))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/tutorials/{tutorialId}/detail")
    public ResponseEntity<TutorialDetailDto> getDetail(@PathVariable Long tutorialId) {
        return relationshipService.getDetail(tutorialId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
