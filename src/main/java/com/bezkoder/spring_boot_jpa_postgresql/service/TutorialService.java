package com.bezkoder.spring_boot_jpa_postgresql.service;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.bezkoder.spring_boot_jpa_postgresql.dto.TutorialDto;

public interface TutorialService {

    Slice<TutorialDto> getAllTutorials(String title, Pageable pageable);

    Optional<TutorialDto> getTutorialById(long id);

    TutorialDto createTutorial(TutorialDto tutorial);

    Optional<TutorialDto> updateTutorial(long id, TutorialDto tutorial);

    void deleteTutorial(long id);

    void deleteAllTutorials();

    Slice<TutorialDto> findByExactTitle(String title, Pageable pageable);

    Slice<TutorialDto> findByPublished(Pageable pageable);
}
