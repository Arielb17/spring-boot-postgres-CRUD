package com.bezkoder.spring_boot_jpa_postgresql.repository;
import com.bezkoder.spring_boot_jpa_postgresql.model.Tutorial;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
    Slice<Tutorial> findAllBy(Pageable pageable);

    Slice<Tutorial> findByPublished(boolean published, Pageable pageable);

    @Query("SELECT t FROM Tutorial t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Slice<Tutorial> findByTitle(@Param("title") String title, Pageable pageable);

    Slice<Tutorial> findByTitleContaining(String title, Pageable pageable);
}
