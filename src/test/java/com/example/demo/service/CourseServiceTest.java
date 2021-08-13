package com.example.demo.service;

import com.example.demo.dao.CourseRepository;
import com.example.demo.domain.Course;
import com.example.demo.exceptions.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

public class CourseServiceTest {
    private final CourseRepository courseRepositoryMock = Mockito.mock(CourseRepository.class);
    private final CourseService courseService = new CourseService(courseRepositoryMock);

    @Test
    void courseByExistingAuthorTest() {
        final Course courseByJack = Course.builder()
                .id(1L)
                .author("Jack")
                .build();
        final Course firstCourseByArtur = Course.builder()
                .id(2L)
                .author("Artur")
                .build();
        final Course secondCourseByArtur = Course.builder()
                .id(3L)
                .author("Artur")
                .build();
        final Course courseByDenis = Course.builder()
                .id(4L)
                .author("Denis")
                .build();
        Mockito.when(courseRepositoryMock.findAll())
                .thenReturn(List.of(firstCourseByArtur, secondCourseByArtur, courseByJack, courseByDenis));

        Assertions.assertTrue(courseService.coursesByAuthor("Jack").contains(courseByJack));
        Assertions.assertEquals(1, courseService.coursesByAuthor("Jack").size());

        Assertions.assertTrue(courseService.coursesByAuthor("Denis").contains(courseByDenis));
        Assertions.assertEquals(1, courseService.coursesByAuthor("Denis").size());
    }

    @Test
    void severalCoursesByExistingAuthorTest() {
        final Course firstCourseByJack = Course.builder()
                .id(1L)
                .author("Jack")
                .build();
        final Course courseByArtur = Course.builder()
                .id(2L)
                .author("Artur")
                .build();
        final Course secondCourseByJack = Course.builder()
                .id(3L)
                .author("Jack")
                .build();
        final Course courseByDenis = Course.builder()
                .id(4L)
                .author("Denis")
                .build();
        final Course thirdCourseByJack = Course.builder()
                .id(5L)
                .author("Jack")
                .build();
        Mockito.when(courseRepositoryMock.findAll()).thenReturn(List.of(courseByArtur, firstCourseByJack, courseByDenis,
                secondCourseByJack, thirdCourseByJack));

        Assertions.assertTrue(courseService.coursesByAuthor("Jack")
                .containsAll(List.of(firstCourseByJack, secondCourseByJack, thirdCourseByJack)));
        Assertions.assertEquals(3, courseService.coursesByAuthor("Jack").size());
    }

    @Test
    void coursesByNotExistingAuthorTest() {
        final Course courseByJack = Course.builder()
                .id(1L)
                .author("Jack")
                .build();
        final Course firstCourseByArtur = Course.builder()
                .id(2L)
                .author("Artur")
                .build();
        final Course secondCourseByArtur = Course.builder()
                .id(3L)
                .author("Artur")
                .build();
        final Course courseByDenis = Course.builder()
                .id(4L)
                .author("Denis")
                .build();
        Mockito.when(courseRepositoryMock.findAll())
                .thenReturn(List.of(firstCourseByArtur, secondCourseByArtur, courseByJack, courseByDenis));

        Assertions.assertEquals(0, courseService.coursesByAuthor("Andrey").size());
    }

    @Test
    void coursesByTitleWithPrefixTest() {
        final Course courseByJack = Course.builder()
                .id(1L)
                .title("CourseByJack")
                .build();
        final Course firstCourseByArtur = Course.builder()
                .id(2L)
                .title("CourseByArtur1")
                .build();
        final Course secondCourseByArtur = Course.builder()
                .id(3L)
                .title("CourseByArtur2")
                .build();
        final Course courseByDenis = Course.builder()
                .id(4L)
                .title("CourseByDenis")
                .build();
        Mockito.when(courseRepositoryMock.findByTitleLike("CourseByJack%")).thenReturn(List.of(courseByJack));
        Mockito.when(courseRepositoryMock.findByTitleLike("CourseByArtur%"))
                .thenReturn(List.of(firstCourseByArtur, secondCourseByArtur));
        Mockito.when(courseRepositoryMock.findByTitleLike("CourseBy%"))
                .thenReturn(List.of(courseByJack, firstCourseByArtur, secondCourseByArtur, courseByDenis));
        Mockito.when(courseRepositoryMock.findByTitleLike("%")).
                thenReturn(List.of(courseByJack, firstCourseByArtur, secondCourseByArtur, courseByDenis));
        ;

        Assertions.assertTrue(courseService.coursesByTitleWithPrefix("CourseByJack").contains(courseByJack));
        Assertions.assertEquals(1, courseService.coursesByTitleWithPrefix("CourseByJack").size());

        Assertions.assertTrue(courseService.coursesByTitleWithPrefix("CourseByArtur")
                .containsAll(List.of(firstCourseByArtur, secondCourseByArtur)));
        Assertions.assertEquals(2, courseService.coursesByTitleWithPrefix("CourseByArtur").size());

        Assertions.assertTrue(courseService.coursesByTitleWithPrefix("CourseBy")
                .containsAll(List.of(courseByJack, firstCourseByArtur, secondCourseByArtur, courseByDenis)));
        Assertions.assertEquals(4, courseService.coursesByTitleWithPrefix("CourseBy").size());

        Assertions.assertTrue(courseService.coursesByTitleWithPrefix("")
                .containsAll(List.of(courseByJack, firstCourseByArtur, secondCourseByArtur, courseByDenis)));
        Assertions.assertEquals(4, courseService.coursesByTitleWithPrefix("").size());
    }

    @Test
    void courseByIdWhenCourseExistTest() {
        final Long existingId = 5L;
        final Long notExistingId = 404L;
        final Course course = Course.builder().id(existingId).build();
        Mockito.when(courseRepositoryMock.findById(existingId)).thenReturn(Optional.of(course));
        Mockito.when(courseRepositoryMock.findById(Mockito.argThat(arg -> !arg.equals(existingId))))
                .thenReturn(Optional.empty());

        Assertions.assertEquals(course, courseService.courseById(existingId));
    }

    @Test
    void courseByIdWhenCourseNotExistTest() {
        final Long existingId = 5L;
        final Long notExistingId = 404L;
        final Course course = Course.builder().id(existingId).build();
        Mockito.when(courseRepositoryMock.findById(existingId)).thenReturn(Optional.of(course));
        Mockito.when(courseRepositoryMock.findById(Mockito.argThat(arg -> !arg.equals(existingId))))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> courseService.courseById(notExistingId));
    }
}
