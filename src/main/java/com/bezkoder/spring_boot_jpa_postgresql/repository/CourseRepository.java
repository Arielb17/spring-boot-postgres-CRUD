package com.bezkoder.spring_boot_jpa_postgresql.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bezkoder.spring_boot_jpa_postgresql.model.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
