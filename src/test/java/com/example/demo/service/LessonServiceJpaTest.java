package com.example.demo.service;

import com.example.demo.dao.CourseRepository;
import com.example.demo.dao.LessonRepository;
import com.example.demo.domain.Course;
import com.example.demo.domain.Lesson;
import com.example.demo.dto.LessonDto;
import com.example.demo.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class LessonServiceJpaTest {
    @Autowired
    private LessonRepository lessonRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private LessonService lessonService;
    @Autowired
    private TestEntityManager entityManager;

    @TestConfiguration
    static class testConfig {
        @Bean
        LessonService LessonService(LessonRepository lessonRepository, CourseRepository courseRepository) {
            return new LessonService(lessonRepository, courseRepository);
        }
    }

    @Test
    void lessonByIdWhenExistTest() {
        Course course = Course.builder()
                .title("testCourse")
                .author("testAuthor")
                .build();
        Lesson lesson = Lesson.builder()
                .title("lessonTitle")
                .text("lessonText")
                .course(course)
                .build();
        course.setLessons(List.of(lesson));
        course = entityManager.persist(course);
        lesson = entityManager.persistAndFlush(lesson);

        LessonDto actualLessonDto = lessonService.lessonById(lesson.getId());
        assertEquals(lesson.getId(), actualLessonDto.getId());
        assertEquals(lesson.getText(), actualLessonDto.getText());
        assertEquals(lesson.getCourse().getId(), actualLessonDto.getCourseId());
    }

    @Test
    void lessonByIdWhenNotExistTest() {
        Course course = Course.builder()
                .title("testCourse")
                .author("testAuthor")
                .build();
        Lesson lesson = Lesson.builder()
                .title("lessonTitle")
                .text("lessonText")
                .course(course)
                .build();
        course.setLessons(List.of(lesson));
        entityManager.persist(course);
        entityManager.persistAndFlush(lesson);

        assertThrows(NotFoundException.class, () -> lessonService.lessonById(-1L));
    }

    @Test
    void saveTest() {
        Course course = Course.builder()
                .title("testCourse")
                .author("testAuthor")
                .build();
        course = entityManager.persistAndFlush(course);
        LessonDto lessonDto = LessonDto.builder()
                .title("lessonTitle")
                .text("lessonText")
                .courseId(course.getId())
                .build();

        LessonDto savedLessonDto = lessonService.save(lessonDto);

        Lesson savedLesson = entityManager.find(Lesson.class, savedLessonDto.getId());
        assertEquals(lessonDto.getTitle(), savedLesson.getTitle());
        assertEquals(lessonDto.getText(), savedLesson.getText());
        assertEquals(lessonDto.getCourseId(), savedLesson.getCourse().getId());
        assertEquals(lessonDto.getTitle(), savedLessonDto.getTitle());
        assertEquals(lessonDto.getText(), savedLessonDto.getText());
        assertEquals(lessonDto.getCourseId(), savedLessonDto.getId());
    }

    @Test
    void deleteLessonByIdTest() {
        Course course = Course.builder()
                .title("testCourse")
                .author("testAuthor")
                .build();
        Lesson lesson = Lesson.builder()
                .title("lessonTitle")
                .text("lessonText")
                .course(course)
                .build();
        course.setLessons(List.of(lesson));
        course = entityManager.persist(course);
        lesson = entityManager.persistAndFlush(lesson);
        assertNotNull(entityManager.find(Lesson.class, lesson.getId()));

        lessonService.deleteLessonById(lesson.getId());
        assertNull(entityManager.find(Lesson.class, lesson.getId()));
    }

    @Test
    void lessonsByCourseWithoutTextTest() {
        Course course = Course.builder()
                .title("testCourse")
                .author("testAuthor")
                .build();
        Lesson firstLesson = Lesson.builder()
                .title("firstLessonTitle")
                .text("firstLessonText")
                .course(course)
                .build();
        Lesson secondLesson = Lesson.builder()
                .title("SecondLessonTitle")
                .text("")
                .course(course)
                .build();
        course.setLessons(List.of(firstLesson, secondLesson));
        course = entityManager.persist(course);
        firstLesson = entityManager.persist(firstLesson);
        secondLesson = entityManager.persistAndFlush(secondLesson);

        List<LessonDto> actualLessonDtoList = lessonService.lessonsByCourseWithoutText(course.getId());

        assertEquals(2, actualLessonDtoList.size());
        assertEquals(firstLesson.getCourse().getId(), actualLessonDtoList.get(0).getCourseId());
        assertEquals(secondLesson.getCourse().getId(), actualLessonDtoList.get(1).getCourseId());
        assertEquals(firstLesson.getId(), actualLessonDtoList.get(0).getId());
        assertEquals(secondLesson.getId(), actualLessonDtoList.get(1).getId());
        assertNull(actualLessonDtoList.get(0).getText());
        assertNull(actualLessonDtoList.get(1).getText());
    }
}
