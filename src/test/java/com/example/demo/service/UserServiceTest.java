package com.example.demo.service;

import com.example.demo.dao.UserRepository;
import com.example.demo.domain.Course;
import com.example.demo.domain.User;
import com.example.demo.dto.UserDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

public class UserServiceTest {
    private final UserRepository userRepositoryMock = Mockito.mock(UserRepository.class);
    private final CourseService courseServiceMock = Mockito.mock(CourseService.class);
    private final PasswordEncoder encoderMock = Mockito.mock(PasswordEncoder.class);
    private final UserService userService = new UserService(userRepositoryMock, courseServiceMock, encoderMock);

    @BeforeEach
    void setUp() {
        Mockito.when(encoderMock.encode("password")).thenReturn("password");
    }

    @Test
    void usersNotAssignedToCourseTest() {
        final Long notExistingCourseId = 2L;
        final Course course = new Course(1L, "author", "title", null, null);
        final User user = new User(1L, "username", "password", Set.of(course), null);
        course.setUsers(Set.of(user));
        List<UserDto> userDtoList = List.of(new UserDto(1L, "username", "", Set.of(course), null));
        Mockito.when(userRepositoryMock.findUsersNotAssignedToCourse(notExistingCourseId)).thenReturn(List.of(user));

        Assertions.assertEquals(List.of(), userService.usersNotAssignedToCourse(course.getId()));

        Assertions.assertEquals(userDtoList, userService.usersNotAssignedToCourse(notExistingCourseId));
    }

    @Test
    void usersAssignedToCourseTest() {
        Course course = new Course(1L, "author", "title", null, null);
        User firstUser = new User(1L, "username", "password", Set.of(course), null);
        User secondUser = new User(2L, "username2", "password2", Set.of(course), null);
        course.setUsers(Set.of(firstUser));
        List<UserDto> userDtoList = List.of(
                new UserDto(1L, "username", "", Set.of(course), null),
                new UserDto(2L, "username", "", Set.of(course), null));
        Mockito.when(userRepositoryMock.findUsersAssignedToCourse(1L)).thenReturn(List.of(firstUser, secondUser));

        Assertions.assertEquals(userDtoList, userService.usersAssignedToCourse(course.getId()));

        final Long notExistingCourseId = 2L;
        Assertions.assertEquals(List.of(), userService.usersAssignedToCourse(notExistingCourseId));
    }
}
