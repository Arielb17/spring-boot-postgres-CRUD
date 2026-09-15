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
        return ResponseEntity.ok(relationshipService.getAuthor(id));
    }

    @PostMapping("/tutorials/{tutorialId}/author/{authorId}")
    public ResponseEntity<TutorialDto> setAuthor(@PathVariable Long tutorialId, @PathVariable Long authorId) {
        return ResponseEntity.ok(relationshipService.setAuthor(tutorialId, authorId));
    }

    @PostMapping("/courses")
    public ResponseEntity<CourseDto> createCourse(@RequestBody CourseDto courseDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(relationshipService.createCourse(courseDto));
    }

    @GetMapping("/courses/{id}")
    public ResponseEntity<CourseDto> getCourse(@PathVariable Long id) {
        return ResponseEntity.ok(relationshipService.getCourse(id));
    }

    @PostMapping("/tutorials/{tutorialId}/courses/{courseId}")
    public ResponseEntity<TutorialDto> addCourse(@PathVariable Long tutorialId, @PathVariable Long courseId) {
        return ResponseEntity.ok(relationshipService.addCourse(tutorialId, courseId));
    }

    @DeleteMapping("/tutorials/{tutorialId}/courses/{courseId}")
    public ResponseEntity<Void> removeCourse(@PathVariable Long tutorialId, @PathVariable Long courseId) {
        relationshipService.removeCourse(tutorialId, courseId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tutorials/{tutorialId}/detail")
    public ResponseEntity<TutorialDetailDto> createDetail(@PathVariable Long tutorialId,
            @RequestBody TutorialDetailDto detailDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(relationshipService.createDetail(tutorialId, detailDto));
    }

    @GetMapping("/tutorials/{tutorialId}/detail")
    public ResponseEntity<TutorialDetailDto> getDetail(@PathVariable Long tutorialId) {
        return ResponseEntity.ok(relationshipService.getDetail(tutorialId));
    }
}
