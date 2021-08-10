package com.example.demo.service;

import com.example.demo.dao.CourseRepository;
import com.example.demo.dao.LessonRepository;
import com.example.demo.domain.Course;
import com.example.demo.domain.Lesson;
import com.example.demo.dto.LessonDto;
import com.example.demo.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LessonService {
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;

    @Autowired
    public LessonService(LessonRepository lessonRepository, CourseRepository courseRepository) {
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
    }

    public LessonDto lessonById(Long id) {
        return lessonRepository.findById(id).map(l -> new LessonDto(l.getId(), l.getTitle(), l.getText(), l.getCourse().getId()))
                .orElseThrow(NotFoundException::new);
    }

    public LessonDto save(LessonDto lessonDto) {
        Course course = courseRepository.getById(lessonDto.getCourseId());
        Lesson lesson = new Lesson(
                lessonDto.getId(),
                lessonDto.getTitle(),
                lessonDto.getText(),
                course
        );
        lesson = lessonRepository.save(lesson);
        return new LessonDto(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getText(),
                lesson.getCourse().getId()
        );
    }

    public void deleteLessonById(Long id) {
        lessonRepository.deleteById(id);
    }

    public List<LessonDto> lessonsByCourseWithoutText(Long courseId) {
        return lessonRepository.findAllByCourseIdWithoutText(courseId);
    }
}
