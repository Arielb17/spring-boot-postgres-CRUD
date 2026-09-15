package com.bezkoder.spring_boot_jpa_postgresql.service.impl;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bezkoder.spring_boot_jpa_postgresql.dto.TutorialDto;
import com.bezkoder.spring_boot_jpa_postgresql.exception.ResourceNotFoundException;
import com.bezkoder.spring_boot_jpa_postgresql.mapper.TutorialMapper;
import com.bezkoder.spring_boot_jpa_postgresql.model.Tutorial;
import com.bezkoder.spring_boot_jpa_postgresql.repository.TutorialRepository;
import com.bezkoder.spring_boot_jpa_postgresql.service.TutorialService;

@Service
public class TutorialServiceImpl implements TutorialService {

    private static final String TUTORIAL_NOT_FOUND = "Tutorial não encontrado.";

    private final TutorialRepository tutorialRepository;
    private final TutorialMapper tutorialMapper;

    public TutorialServiceImpl(TutorialRepository tutorialRepository, TutorialMapper tutorialMapper) {
        this.tutorialRepository = tutorialRepository;
        this.tutorialMapper = tutorialMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<TutorialDto> getAllTutorials(String title, Pageable pageable) {
        if (title == null) {
            return tutorialRepository.findAllBy(pageable).map(tutorialMapper::toDto);
        }

        return tutorialRepository.findByTitleContaining(title, pageable).map(tutorialMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public TutorialDto getTutorialById(long id) {
        return tutorialMapper.toDto(findTutorial(id));
    }

    @Override
    @Transactional
    public TutorialDto createTutorial(TutorialDto tutorial) {
        Tutorial newTutorial = tutorialMapper.toEntity(tutorial);
        newTutorial.setPublished(false);
        return tutorialMapper.toDto(tutorialRepository.save(newTutorial));
    }

    @Override
    @Transactional
    public TutorialDto updateTutorial(long id, TutorialDto tutorial) {
        Tutorial existingTutorial = findTutorial(id);
        tutorialMapper.updateEntityFromDto(tutorial, existingTutorial);
        return tutorialMapper.toDto(tutorialRepository.save(existingTutorial));
    }

    @Override
    @Transactional
    public void deleteTutorial(long id) {
        tutorialRepository.delete(findTutorial(id));
    }

    @Override
    public void deleteAllTutorials() {
        tutorialRepository.deleteAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<TutorialDto> findByExactTitle(String title, Pageable pageable) {
        return tutorialRepository.findByTitle(title, pageable).map(tutorialMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<TutorialDto> findByPublished(Pageable pageable) {
        return tutorialRepository.findByPublished(true, pageable).map(tutorialMapper::toDto);
    }

    private Tutorial findTutorial(long id) {
        return tutorialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TUTORIAL_NOT_FOUND));
    }
}
