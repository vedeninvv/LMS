package com.example.demo.service;

import com.example.demo.controller.NotFoundException;
import com.example.demo.dao.CourseRepository;
import com.example.demo.domain.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CourseLister {
    private final CourseRepository repository;

    @Autowired
    public CourseLister(CourseRepository repository) {
        this.repository = repository;
    }

    public List<Course> coursesByAuthor(String name) {
        List<Course> allCourses = repository.findAll();
        return allCourses.stream().filter(course -> course.getAuthor().equals(name)).collect(Collectors.toList());
    }

    public List<Course> coursesByTitleWithPrefix(String prefix) {
        return repository.findByTitleWithPrefix(prefix == null ? "" : prefix);
    }

    public Course courseById(long id) {
        return repository.findById(id).orElseThrow(NotFoundException::new);
    }

    public void save(Course course) {
        repository.save(course);
    }

    public void delete(long id) {
        repository.delete(id);
    }
}
