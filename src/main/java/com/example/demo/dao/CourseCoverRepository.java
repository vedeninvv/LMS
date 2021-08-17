package com.example.demo.dao;

import com.example.demo.domain.Course;
import com.example.demo.domain.CourseCover;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseCoverRepository extends JpaRepository<CourseCover, Long> {
    Optional<CourseCover> findByCourse(Course course);
}
