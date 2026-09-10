package com.bezkoder.spring_boot_jpa_postgresql.service;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.bezkoder.spring_boot_jpa_postgresql.model.Tutorial;

public interface TutorialService {

    Slice<Tutorial> getAllTutorials(String title, Pageable pageable);

    Optional<Tutorial> getTutorialById(long id);

    Tutorial createTutorial(Tutorial tutorial);

    Optional<Tutorial> updateTutorial(long id, Tutorial tutorial);

    void deleteTutorial(long id);

    void deleteAllTutorials();

    Slice<Tutorial> findByExactTitle(String title, Pageable pageable);

    Slice<Tutorial> findByPublished(Pageable pageable);
}
