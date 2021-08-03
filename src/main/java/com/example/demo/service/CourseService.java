package com.example.demo.service;

import com.example.demo.dto.UserDto;
import com.example.demo.exceptions.NotFoundException;
import com.example.demo.dao.CourseRepository;
import com.example.demo.domain.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {
    private final CourseRepository repository;

    @Autowired
    public CourseService(CourseRepository repository) {
        this.repository = repository;
    }

    public List<Course> coursesByAuthor(String name) {
        List<Course> allCourses = repository.findAll();
        return allCourses.stream().filter(course -> course.getAuthor().equals(name)).collect(Collectors.toList());
    }

    public List<Course> coursesByTitleWithPrefix(String prefix) {
        return repository.findByTitleLike(prefix == null ? "%" : prefix + "%");
    }

    public Course courseById(long id) {
        return repository.findById(id).orElseThrow(NotFoundException::new);
    }

    public boolean hasCourseById(long id){
        return repository.findById(id).isPresent();
    }

    public void save(Course course) {
        repository.save(course);
    }

    public void delete(long id) {
        repository.delete(repository.getById(id));
    }
}
