package com.example.demo.service;

import com.example.demo.dao.CourseRepository;
import com.example.demo.dao.LessonRepository;
import com.example.demo.domain.Course;
import com.example.demo.domain.Lesson;
import com.example.demo.dto.LessonDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LessonLister {
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;

    @Autowired
    public LessonLister(LessonRepository lessonRepository, CourseRepository courseRepository) {
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
    }

    public LessonDto lessonById(Long id) {
        //todo fix to throw exception
        return lessonRepository.findById(id).map(l -> new LessonDto(l.getId(), l.getTitle(), l.getText(), l.getCourse().getId())).get();
    }

    public void save(LessonDto lessonDto){
        Course course = courseRepository.getById(lessonDto.getCourseId());
        Lesson lesson = new Lesson(
                lessonDto.getId(),
                lessonDto.getTitle(),
                lessonDto.getText(),
                course
        );
        lessonRepository.save(lesson);
    }

    public void deleteLessonById(Long id){
        lessonRepository.deleteById(id);
    }
}
