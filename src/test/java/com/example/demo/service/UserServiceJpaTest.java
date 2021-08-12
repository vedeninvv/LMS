package com.example.demo.service;

import com.example.demo.dao.UserRepository;
import com.example.demo.domain.Course;
import com.example.demo.domain.User;
import com.example.demo.dto.UserDto;
import com.example.demo.exceptions.NotFoundException;
import com.example.demo.exceptions.UsernameExistException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
public class UserServiceJpaTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestEntityManager entityManager;
    @MockBean
    private PasswordEncoder encoderMock;
    @MockBean
    private CourseService courseServiceMock;
    @Autowired
    private UserService userService;

    @TestConfiguration
    static class testConfig {
        @Bean
        UserService userService(UserRepository userRepository, PasswordEncoder encoder, CourseService courseService) {
            return new UserService(userRepository, courseService, encoder);
        }
    }

    @Test
    void userByIdWhenUserExistTest() {
        User userArtur = User.builder()
                .username("usernameArtur")
                .password("passwordArtur")
                .build();
        userArtur = entityManager.persistAndFlush(userArtur);

        assertEquals(userArtur.getId(), userService.userById(userArtur.getId()).getId());
        assertEquals(userArtur.getUsername(), userService.userById(userArtur.getId()).getUsername());
        assertEquals("", userService.userById(userArtur.getId()).getPassword());
    }

    @Test
    void userByIdWhenUserNotExistTest() {
        User userArtur = User.builder()
                .username("usernameArtur")
                .password("passwordArtur")
                .build();
        entityManager.persistAndFlush(userArtur);

        assertThrows(NotFoundException.class, () -> userService.userById(-1L));
    }

    @Test
    void findAllTest() {
        User userArtur = User.builder()
                .username("usernameArtur")
                .password("passwordArtur")
                .build();
        userArtur = entityManager.persistAndFlush(userArtur);

        User userOleg = User.builder()
                .username("usernameOleg")
                .password("passwordOleg")
                .build();
        userOleg = entityManager.persistAndFlush(userOleg);

        List<Long> expectedUserIdList = List.of(userArtur.getId(), userOleg.getId());
        List<String> expectedUserUsernameList = List.of(userArtur.getUsername(), userOleg.getUsername());

        List<UserDto> actualUserDtoList = userService.findAll();
        List<Long> actualUserIdList = actualUserDtoList.stream().map(UserDto::getId).collect(Collectors.toList());
        List<String> actualUserUsernameList = actualUserDtoList.stream().map(UserDto::getUsername).collect(Collectors.toList());

        assertEquals(expectedUserIdList.size(), actualUserIdList.size());
        assertTrue(actualUserIdList.containsAll(expectedUserIdList));

        assertEquals(expectedUserUsernameList.size(), actualUserUsernameList.size());
        assertTrue(actualUserUsernameList.containsAll(expectedUserUsernameList));
    }

    @Test
    void findUserByUsernameWhenUserExistTest() {
        User userArtur = User.builder()
                .username("usernameArtur")
                .password("passwordArtur")
                .build();
        userArtur = entityManager.persistAndFlush(userArtur);

        assertEquals(userArtur.getId(), userService.findByUsername(userArtur.getUsername()).getId());
        assertEquals(userArtur.getUsername(), userService.findByUsername(userArtur.getUsername()).getUsername());
        assertEquals("", userService.findByUsername(userArtur.getUsername()).getPassword());
    }

    @Test
    void findUserByUsernameWhenUserNotExistTest() {
        User userArtur = User.builder()
                .username("usernameArtur")
                .password("passwordArtur")
                .build();
        entityManager.persistAndFlush(userArtur);

        assertThrows(NotFoundException.class, () -> userService.findByUsername("notExist"));
    }

    @Test
    void deleteByIdWhenUserExistTest() {
        User userArtur = User.builder()
                .username("usernameArtur")
                .password("passwordArtur")
                .build();
        userArtur = entityManager.persistAndFlush(userArtur);

        userService.deleteById(userArtur.getId());

        assertNull(entityManager.find(User.class, userArtur.getId()));
    }

    @Test
    void deleteByIdWhenUserNotExistTest() {
        User userArtur = User.builder()
                .username("usernameArtur")
                .password("passwordArtur")
                .build();
        entityManager.persistAndFlush(userArtur);

        assertThrows(NotFoundException.class, () -> userService.deleteById(-1L));
    }

    @Test
    void saveWhenUsernameFreeTest() {
        Mockito.when(encoderMock.encode("passwordArtur")).thenReturn("passwordArtur");
        UserDto userDtoArtur = UserDto.builder()
                .username("usernameArtur")
                .password("passwordArtur")
                .build();

        UserDto userDtoArturAfterSave = userService.save(userDtoArtur);
        User userArtur = entityManager.find(User.class, userDtoArturAfterSave.getId());

        assertNotNull(userArtur);
        assertEquals(userDtoArtur.getUsername(), userArtur.getUsername());
        assertEquals(userDtoArtur.getPassword(), userArtur.getPassword());
        assertEquals(userDtoArtur.getUsername(), userDtoArturAfterSave.getUsername());
        assertEquals("", userDtoArturAfterSave.getPassword());
    }

    @Test
    void saveWhenUsernameNotFreeTest() {
        Mockito.when(encoderMock.encode("passwordArtur")).thenReturn("passwordArtur");
        UserDto userDtoArtur = UserDto.builder()
                .username("usernameArtur")
                .password("passwordArtur")
                .build();

        userService.save(userDtoArtur);

        assertThrows(UsernameExistException.class, () -> userService.save(userDtoArtur));
    }

    @Test
    void updateUsernameAndPasswordWhenNewUsernameFreeTest() {
        Mockito.when(encoderMock.encode("newPasswordArtur")).thenReturn("newPasswordArtur");
        User userArtur = User.builder()
                .username("usernameArtur")
                .password("passwordArtur")
                .build();
        UserDto userDtoArturWithNewUsername = UserDto.builder()
                .username("newUsernameArtur")
                .password("newPasswordArtur")
                .build();
        userArtur = entityManager.persistAndFlush(userArtur);
        userDtoArturWithNewUsername.setId(userArtur.getId());

        UserDto userDtoArturAfterUpdate = userService.updateUsernameAndPassword(userDtoArturWithNewUsername);

        assertEquals(userDtoArturWithNewUsername.getUsername(), userDtoArturAfterUpdate.getUsername());
        assertEquals(userDtoArturWithNewUsername.getPassword(), userDtoArturAfterUpdate.getPassword());
        assertEquals(userDtoArturWithNewUsername.getId(), userDtoArturAfterUpdate.getId());

        userArtur = entityManager.find(User.class, userArtur.getId());
        assertEquals(userDtoArturWithNewUsername.getUsername(), userArtur.getUsername());
        assertEquals(userDtoArturWithNewUsername.getPassword(), userArtur.getPassword());
        assertEquals(userDtoArturWithNewUsername.getId(), userArtur.getId());
    }

    @Test
    void updateUsernameAndPasswordWhenNewUsernameNotFreeTest() {
        Mockito.when(encoderMock.encode("passwordArtur")).thenReturn("passwordArtur");
        User userArtur = User.builder()
                .username("usernameArtur")
                .password("passwordArtur")
                .build();
        UserDto userDtoArturWithNewUsername = UserDto.builder()
                .username("usernameArtur")
                .password("passwordArtur")
                .build();
        userArtur = entityManager.persistAndFlush(userArtur);
        userDtoArturWithNewUsername.setId(userArtur.getId());

        assertThrows(UsernameExistException.class, () -> userService.updateUsernameAndPassword(userDtoArturWithNewUsername));
    }

    @Test
    void updateUsernameAndPasswordWhenNewUserNotExistTest() {
        Mockito.when(encoderMock.encode("newPasswordArtur")).thenReturn("newPasswordArtur");
        User userArtur = User.builder()
                .username("usernameArtur")
                .password("passwordArtur")
                .build();
        UserDto userDtoArturWithNewUsername = UserDto.builder()
                .username("newUsernameArtur")
                .password("newPasswordArtur")
                .build();
        entityManager.persistAndFlush(userArtur);
        userDtoArturWithNewUsername.setId(-1L);

        assertThrows(NotFoundException.class, () -> userService.updateUsernameAndPassword(userDtoArturWithNewUsername));
    }

    @Test
    void assignCourseToUserTest() {
        Mockito.when(encoderMock.encode("newPasswordArtur")).thenReturn("newPasswordArtur");
        User userArtur = User.builder()
                .username("usernameArtur")
                .password("passwordArtur")
                .courses(new HashSet<>())
                .build();
        userArtur = entityManager.persistAndFlush(userArtur);
        Course course = Course.builder()
                .title("courseTitle")
                .author("Artur")
                .users(new HashSet<>())
                .build();
        course = entityManager.persistAndFlush(course);
        Mockito.when(courseServiceMock.courseById(course.getId())).thenReturn(course);

        userService.assignCourseToUser(userArtur.getId(), course.getId());

        userArtur = entityManager.find(User.class, userArtur.getId());
        course = entityManager.find(Course.class, course.getId());
        assertTrue(userArtur.getCourses().contains(course));
        assertTrue(course.getUsers().contains(userArtur));
    }

    @Test
    void takeOffCourseTest() {
        Mockito.when(encoderMock.encode("newPasswordArtur")).thenReturn("newPasswordArtur");
        User userArtur = User.builder()
                .username("usernameArtur")
                .password("passwordArtur")
                .courses(new HashSet<>())
                .build();
        Course course = Course.builder()
                .title("courseTitle")
                .author("Artur")
                .users(new HashSet<>())
                .build();
        userArtur.getCourses().add(course);
        course.getUsers().add(userArtur);
        userArtur = entityManager.persist(userArtur);
        course = entityManager.persistAndFlush(course);
        Mockito.when(courseServiceMock.courseById(course.getId())).thenReturn(course);

        userArtur = entityManager.find(User.class, userArtur.getId());
        course = entityManager.find(Course.class, course.getId());
        assertEquals(course, userArtur.getCourses().toArray()[0]);
        assertEquals(userArtur, course.getUsers().toArray()[0]);

        userService.takeOffCourse(userArtur.getId(), course.getId());

        userArtur = entityManager.find(User.class, userArtur.getId());
        course = entityManager.find(Course.class, course.getId());
        assertTrue(userArtur.getCourses().isEmpty());
        assertTrue(course.getUsers().isEmpty());
    }
}
