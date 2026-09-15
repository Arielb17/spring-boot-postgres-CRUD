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

import com.bezkoder.spring_boot_jpa_postgresql.dto.TutorialDto;
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
    public ResponseEntity<Slice<TutorialDto>> getAllTutorials(@RequestParam(required = false) String title,
            @SortDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Slice<TutorialDto> tutorials = tutorialService.getAllTutorials(title, pageable);
        return ResponseEntity.ok(tutorials);
    }

    @GetMapping("/tutorials/by-title")
    public ResponseEntity<Slice<TutorialDto>> findByExactTitle(@RequestParam String title,
            @SortDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Slice<TutorialDto> tutorials = tutorialService.findByExactTitle(title, pageable);
        return ResponseEntity.ok(tutorials);
    }

    @GetMapping("/tutorials/{id}")
    public ResponseEntity<TutorialDto> getTutorialById(@PathVariable("id") long id) {
        return ResponseEntity.ok(tutorialService.getTutorialById(id));
    }

    @PostMapping("/tutorials")
    public ResponseEntity<TutorialDto> createTutorial(@RequestBody TutorialDto tutorial) {
        TutorialDto createdTutorial = tutorialService.createTutorial(tutorial);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTutorial);
    }

    @PutMapping("/tutorials/{id}")
    public ResponseEntity<TutorialDto> updateTutorial(@PathVariable("id") long id, @RequestBody TutorialDto tutorial) {
        return ResponseEntity.ok(tutorialService.updateTutorial(id, tutorial));
    }

    @DeleteMapping("/tutorials/{id}")
    public ResponseEntity<Void> deleteTutorial(@PathVariable("id") long id) {
        tutorialService.deleteTutorial(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/tutorials")
    public ResponseEntity<Void> deleteAllTutorials() {
        tutorialService.deleteAllTutorials();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tutorials/published")
    public ResponseEntity<Slice<TutorialDto>> findByPublished(
            @SortDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Slice<TutorialDto> tutorials = tutorialService.findByPublished(pageable);
        return ResponseEntity.ok(tutorials);
    }
}
