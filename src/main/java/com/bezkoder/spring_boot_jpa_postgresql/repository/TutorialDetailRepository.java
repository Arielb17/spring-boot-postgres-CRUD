package com.bezkoder.spring_boot_jpa_postgresql.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bezkoder.spring_boot_jpa_postgresql.model.TutorialDetail;

public interface TutorialDetailRepository extends JpaRepository<TutorialDetail, Long> {
    Optional<TutorialDetail> findByTutorialId(Long tutorialId);
}
