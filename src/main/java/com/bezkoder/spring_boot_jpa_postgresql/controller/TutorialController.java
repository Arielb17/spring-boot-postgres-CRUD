package com.bezkoder.spring_boot_jpa_postgresql.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bezkoder.spring_boot_jpa_postgresql.model.Tutorial;
import com.bezkoder.spring_boot_jpa_postgresql.service.TutorialService;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api")
public class TutorialController {

    private final TutorialService tutorialService;

    public TutorialController(TutorialService tutorialService) {
        this.tutorialService = tutorialService;
    }

    @GetMapping("/tutorials")
    public ResponseEntity<Slice<Tutorial>> getAllTutorials(@RequestParam(required = false) String title,
            @SortDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        try {
            Slice<Tutorial> tutorials = tutorialService.getAllTutorials(title, pageable);
            return ResponseEntity.ok(tutorials);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/tutorials/by-title")
    public ResponseEntity<Slice<Tutorial>> findByExactTitle(@RequestParam String title,
            @SortDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        try {
            Slice<Tutorial> tutorials = tutorialService.findByExactTitle(title, pageable);
            return ResponseEntity.ok(tutorials);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/tutorials/{id}")
    public ResponseEntity<Tutorial> getTutorialById(@PathVariable("id") long id) {
        return tutorialService.getTutorialById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/tutorials")
    public ResponseEntity<Tutorial> createTutorial(@RequestBody Tutorial tutorial) {
        try {
            Tutorial createdTutorial = tutorialService.createTutorial(tutorial);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTutorial);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/tutorials/{id}")
    public ResponseEntity<Tutorial> updateTutorial(@PathVariable("id") long id, @RequestBody Tutorial tutorial) {
        return tutorialService.updateTutorial(id, tutorial)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/tutorials/{id}")
    public ResponseEntity<Void> deleteTutorial(@PathVariable("id") long id) {
        try {
            tutorialService.deleteTutorial(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/tutorials")
    public ResponseEntity<Void> deleteAllTutorials() {
        try {
            tutorialService.deleteAllTutorials();
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/tutorials/published")
    public ResponseEntity<Slice<Tutorial>> findByPublished(
            @SortDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        try {
            Slice<Tutorial> tutorials = tutorialService.findByPublished(pageable);
            return ResponseEntity.ok(tutorials);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
