package com.bezkoder.spring_boot_jpa_postgresql.service.impl;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.bezkoder.spring_boot_jpa_postgresql.model.Tutorial;
import com.bezkoder.spring_boot_jpa_postgresql.repository.TutorialRepository;
import com.bezkoder.spring_boot_jpa_postgresql.service.TutorialService;

@Service
public class TutorialServiceImpl implements TutorialService {

    private final TutorialRepository tutorialRepository;

    public TutorialServiceImpl(TutorialRepository tutorialRepository) {
        this.tutorialRepository = tutorialRepository;
    }

    @Override
    public Slice<Tutorial> getAllTutorials(String title, Pageable pageable) {
        if (title == null) {
            return tutorialRepository.findAllBy(pageable);
        }

        return tutorialRepository.findByTitleContaining(title, pageable);
    }

    @Override
    public Optional<Tutorial> getTutorialById(long id) {
        return tutorialRepository.findById(id);
    }

    @Override
    public Tutorial createTutorial(Tutorial tutorial) {
        Tutorial newTutorial = new Tutorial(tutorial.getTitle(), tutorial.getDescription(), false);
        return tutorialRepository.save(newTutorial);
    }

    @Override
    public Optional<Tutorial> updateTutorial(long id, Tutorial tutorial) {
        Optional<Tutorial> tutorialData = tutorialRepository.findById(id);

        if (tutorialData.isEmpty()) {
            return Optional.empty();
        }

        Tutorial existingTutorial = tutorialData.get();
        existingTutorial.setTitle(tutorial.getTitle());
        existingTutorial.setDescription(tutorial.getDescription());
        existingTutorial.setPublished(tutorial.isPublished());

        return Optional.of(tutorialRepository.save(existingTutorial));
    }

    @Override
    public void deleteTutorial(long id) {
        tutorialRepository.deleteById(id);
    }

    @Override
    public void deleteAllTutorials() {
        tutorialRepository.deleteAll();
    }

    @Override
    public Slice<Tutorial> findByExactTitle(String title, Pageable pageable) {
        return tutorialRepository.findByTitle(title, pageable);
    }

    @Override
    public Slice<Tutorial> findByPublished(Pageable pageable) {
        return tutorialRepository.findByPublished(true, pageable);
    }
}
