package com.example.demo.controller;

import com.example.demo.domain.Course;
import com.example.demo.domain.User;
import com.example.demo.dto.UserDto;
import com.example.demo.exceptions.NotFoundException;
import com.example.demo.service.CourseService;
import com.example.demo.service.LessonService;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
public class CourseControllerTest {
    @MockBean
    private CourseService courseService;
    @MockBean
    private UserService userService;
    @MockBean
    private LessonService lessonService;
    @Autowired
    MockMvc mockMvc;

    private Course course;

    @BeforeEach
    void setUp() {
        course = Course.builder()
                .title("prefixCourse")
                .author("AAuthor")
                .users(Set.of())
                .lessons(List.of())
                .id(1L)
                .build();
    }

    @Test
    void courseTableTest() throws Exception {
        final String titlePrefix = "prefix";
        List<Course> coursesList = List.of(course);
        when(courseService.coursesByTitleWithPrefix(titlePrefix)).thenReturn(coursesList);
        mockMvc.perform(get("/course").queryParam("titlePrefix", titlePrefix))
                .andExpect(status().isOk())
                .andExpect(model().attribute("courses", coursesList));
    }

    @Test
    @WithMockUser
    void courseFormTest() throws Exception {
        when(courseService.courseById(course.getId())).thenReturn(course);
        when(lessonService.lessonsByCourseWithoutText(course.getId())).thenReturn(List.of());
        when(userService.usersAssignedToCourse(course.getId())).thenReturn(List.of());
        mockMvc.perform(get("/course/{id}", course.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("course", "lessons", "users", "activePage"))
                .andExpect(model().attribute("course", course));
        verify(courseService, times(1)).courseById(course.getId());
        verify(lessonService, times(1)).lessonsByCourseWithoutText(course.getId());
        verify(userService, times(1)).usersAssignedToCourse(course.getId());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void submitCourseFormTest() throws Exception {
        when(courseService.hasCourseById(course.getId())).thenReturn(true);
        when(courseService.courseById(course.getId())).thenReturn(course);
        doNothing().when(courseService).save(course);
        mockMvc.perform(post("/course").with(csrf()).flashAttr("course", course))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/course"));
        verify(courseService, times(1)).hasCourseById(course.getId());
        verify(courseService, times(1)).courseById(course.getId());
        verify(courseService, times(1)).save(course);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void submitCourseFormInvalidDataTest() throws Exception {
        course.setTitle("");
        mockMvc.perform(post("/course").with(csrf()).flashAttr("course", course))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void submitCourseFormCourseIdIsNullTest() throws Exception {
        course.setId(null);
        doNothing().when(courseService).save(course);
        mockMvc.perform(post("/course").with(csrf()).flashAttr("course", course))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/course"));
        verify(courseService, times(0)).hasCourseById(anyLong());
        verify(courseService, times(0)).courseById(anyLong());
        verify(courseService, times(1)).save(course);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void submitCourseFormCourseNotExistTest() throws Exception {
        when(courseService.hasCourseById(course.getId())).thenReturn(false);
        doNothing().when(courseService).save(course);
        mockMvc.perform(post("/course").with(csrf()).flashAttr("course", course))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/course"));
        verify(courseService, times(1)).hasCourseById(course.getId());
        verify(courseService, times(0)).courseById(anyLong());
        verify(courseService, times(1)).save(course);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void courseFormNewCourseTest() throws Exception {
        mockMvc.perform(get("/course/new"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("course", "activePage"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCourseTest() throws Exception {
        doNothing().when(courseService).delete(course.getId());
        mockMvc.perform(delete("/course/{id}", course.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/course"));
        verify(courseService, times(1)).delete(course.getId());
    }

    @Test
    void notFoundExceptionHandlerTest() throws Exception {
        when(courseService.courseById(course.getId())).thenThrow(NotFoundException.class);
        when(lessonService.lessonsByCourseWithoutText(course.getId())).thenReturn(List.of());
        when(userService.usersAssignedToCourse(course.getId())).thenReturn(List.of());
        mockMvc.perform(get("/course/{id}", course.getId()))
                .andExpect(status().isNotFound())
                .andExpect(view().name("NotFoundException"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignCourseInAdminTest() throws Exception {
        when(courseService.coursesByTitleWithPrefix(anyString())).thenReturn(List.of(course));
        when(userService.usersNotAssignedToCourse(course.getId())).thenReturn(List.of());
        mockMvc.perform(get("/course/{id}/assign", course.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("assignForm", "courses", "users", "courseId"))
                .andExpect(model().attribute("assignForm", "show"));
        verify(userService, times(1)).usersNotAssignedToCourse(course.getId());
    }

    @Test
    @WithMockUser(roles = "STUDENT", username = "username")
    void assignCourseInStudentWhenAlreadyAssignedTest() throws Exception {
        User user = User.builder()
                .username("username")
                .password("password")
                .id(1L)
                .courses(Set.of(course))
                .build();
        UserDto userDto = UserDto.builder()
                .username("username")
                .password("password")
                .id(1L)
                .courses(Set.of(course))
                .build();
        course.setUsers(Set.of(user));
        when(courseService.coursesByTitleWithPrefix(anyString())).thenReturn(List.of(course));
        when(userService.findByUsername(user.getUsername())).thenReturn(userDto);

        mockMvc.perform(get("/course/{id}/assign", course.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("assignForm", "courses", "users", "courseId"))
                .andExpect(model().attribute("assignForm", "show"))
                .andExpect(model().attribute("users", new ArrayList<UserDto>()));
        verify(userService, times(1)).findByUsername(user.getUsername());
    }

    @Test
    @WithMockUser(roles = "STUDENT", username = "username")
    void assignCourseInStudentWhenNotAssignedTest() throws Exception {
        User user = User.builder()
                .username("username")
                .password("password")
                .id(1L)
                .courses(Set.of())
                .build();
        UserDto userDto = UserDto.builder()
                .username("username")
                .password("password")
                .id(1L)
                .courses(Set.of())
                .build();
        course.setUsers(Set.of(user));
        when(courseService.coursesByTitleWithPrefix(anyString())).thenReturn(List.of(course));
        when(userService.findByUsername(user.getUsername())).thenReturn(userDto);
        ArrayList<UserDto> userList = new ArrayList<>();
        userList.add(userDto);

        mockMvc.perform(get("/course/{id}/assign", course.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("assignForm", "courses", "users", "courseId"))
                .andExpect(model().attribute("assignForm", "show"))
                .andExpect(model().attribute("users", userList));
        verify(userService, times(1)).findByUsername(user.getUsername());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignUserFormInAdminTest() throws Exception {
        doNothing().when(userService).assignCourseToUser(1L, course.getId());
        mockMvc.perform(post("/course/{courseId}/assign", course.getId()).with(csrf())
                .queryParam("userId", String.valueOf(1L)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/course"));
        verify(userService, times(1)).assignCourseToUser(1L, course.getId());
    }

    @Test
    @WithMockUser(roles = "STUDENT", username = "username")
    void assignUserFormInStudentTest() throws Exception {
        UserDto userDto = UserDto.builder()
                .username("username")
                .password("password")
                .id(1L)
                .build();
        when(userService.findByUsername(userDto.getUsername())).thenReturn(userDto);
        doNothing().when(userService).assignCourseToUser(userDto.getId(), course.getId());
        mockMvc.perform(post("/course/{courseId}/assign", course.getId()).with(csrf())
                .queryParam("userId", String.valueOf(userDto.getId())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/course"));
        verify(userService, times(1)).assignCourseToUser(userDto.getId(), course.getId());
    }

    @Test
    @WithMockUser(roles = "STUDENT", username = "username")
    void assignUserFormInStudentNotAllowedTest() throws Exception {
        UserDto userDto = UserDto.builder()
                .username("username")
                .password("password")
                .id(1L)
                .build();
        when(userService.findByUsername(userDto.getUsername())).thenReturn(userDto);
        doNothing().when(userService).assignCourseToUser(userDto.getId(), course.getId());
        mockMvc.perform(post("/course/{courseId}/assign", course.getId()).with(csrf())
                .queryParam("userId", String.valueOf(2L)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/course"));
        verify(userService, times(0)).assignCourseToUser(userDto.getId(), course.getId());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void takeOffUserFormInAdminTest() throws Exception {
        doNothing().when(userService).takeOffCourse(1L, course.getId());
        mockMvc.perform(post("/course/{courseId}/take_off/{userId}", course.getId(), 1L).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/course/1"));
        verify(userService, times(1)).takeOffCourse(1L, course.getId());
    }

    @Test
    @WithMockUser(roles = "STUDENT", username = "username")
    void takeOffUserFormInStudentTest() throws Exception {
        UserDto userDto = UserDto.builder()
                .username("username")
                .password("password")
                .id(1L)
                .build();
        when(userService.findByUsername(userDto.getUsername())).thenReturn(userDto);
        doNothing().when(userService).takeOffCourse(userDto.getId(), course.getId());
        mockMvc.perform(post("/course/{courseId}/take_off/{userId}",
                course.getId(), userDto.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/course/1"));
        verify(userService, times(1)).takeOffCourse(userDto.getId(), course.getId());
    }

    @Test
    @WithMockUser(roles = "STUDENT", username = "username")
    void takeOffUserFormInStudentNotAllowedTest() throws Exception {
        UserDto userDto = UserDto.builder()
                .username("username")
                .password("password")
                .id(1L)
                .build();
        when(userService.findByUsername(userDto.getUsername())).thenReturn(userDto);
        doNothing().when(userService).takeOffCourse(userDto.getId(), course.getId());
        mockMvc.perform(post("/course/{courseId}/take_off/{userId}",
                course.getId(), 2L).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/course/1"));
        verify(userService, times(0)).takeOffCourse(userDto.getId(), course.getId());
    }
}
