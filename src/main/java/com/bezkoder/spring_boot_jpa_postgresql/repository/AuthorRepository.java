package com.bezkoder.spring_boot_jpa_postgresql.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bezkoder.spring_boot_jpa_postgresql.model.Author;

public interface AuthorRepository extends JpaRepository<Author, Long> {
}
