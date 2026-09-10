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

import com.bezkoder.spring_boot_jpa_postgresql.model.Author;
import com.bezkoder.spring_boot_jpa_postgresql.model.Course;
import com.bezkoder.spring_boot_jpa_postgresql.model.Tutorial;
import com.bezkoder.spring_boot_jpa_postgresql.model.TutorialDetail;
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
    public ResponseEntity<Author> createAuthor(@RequestBody NameRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(relationshipService.createAuthor(request.name()));
    }

    @GetMapping("/authors/{id}")
    public ResponseEntity<Author> getAuthor(@PathVariable Long id) {
        return relationshipService.getAuthor(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/tutorials/{tutorialId}/author/{authorId}")
    public ResponseEntity<Tutorial> setAuthor(@PathVariable Long tutorialId, @PathVariable Long authorId) {
        return relationshipService.setAuthor(tutorialId, authorId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/courses")
    public ResponseEntity<Course> createCourse(@RequestBody NameRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(relationshipService.createCourse(request.name()));
    }

    @GetMapping("/courses/{id}")
    public ResponseEntity<Course> getCourse(@PathVariable Long id) {
        return relationshipService.getCourse(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/tutorials/{tutorialId}/courses/{courseId}")
    public ResponseEntity<Tutorial> addCourse(@PathVariable Long tutorialId, @PathVariable Long courseId) {
        return relationshipService.addCourse(tutorialId, courseId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/tutorials/{tutorialId}/courses/{courseId}")
    public ResponseEntity<Void> removeCourse(@PathVariable Long tutorialId, @PathVariable Long courseId) {
        return relationshipService.removeCourse(tutorialId, courseId).map(ignored -> ResponseEntity.noContent().<Void>build())
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/tutorials/{tutorialId}/detail")
    public ResponseEntity<TutorialDetail> createDetail(@PathVariable Long tutorialId,
            @RequestBody DetailRequest request) {
        return relationshipService.createDetail(tutorialId, request.content(), request.estimatedMinutes())
                .map(detail -> ResponseEntity.status(HttpStatus.CREATED).body(detail))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/tutorials/{tutorialId}/detail")
    public ResponseEntity<TutorialDetail> getDetail(@PathVariable Long tutorialId) {
        return relationshipService.getDetail(tutorialId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record NameRequest(String name) { }
    public record DetailRequest(String content, Integer estimatedMinutes) { }
}
