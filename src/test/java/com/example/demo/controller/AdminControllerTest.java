package com.example.demo.controller;

import com.example.demo.domain.Role;
import com.example.demo.dto.UserDto;
import com.example.demo.exceptions.UsernameExistException;
import com.example.demo.service.RoleService;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
public class AdminControllerTest {
    @MockBean
    private RoleService roleService;
    @MockBean
    private UserService userService;
    @Autowired
    MockMvc mockMvc;

    private Role roleStudent;
    private Role roleAdmin;

    @BeforeEach
    void setUp() {
        roleStudent = Role.builder()
                .name("ROLE_STUDENT")
                .id(2L)
                .build();
        roleAdmin = Role.builder()
                .name("ROLE_ADMIN")
                .id(1L)
                .build();
        when(roleService.findAll()).thenReturn(List.of(roleAdmin, roleStudent));
        when(roleService.findRoleByName(roleAdmin.getName())).thenReturn(roleAdmin);
        when(roleService.findRoleByName(roleStudent.getName())).thenReturn(roleStudent);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminPanelTest() throws Exception {
        when(userService.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("username", "userDto", "roles", "allUsers"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUserTest() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .username("username")
                .password("password")
                .roles(Set.of(roleStudent))
                .build();
        when(userService.save(userDto)).thenReturn(userDto);
        mockMvc.perform(post("/admin/new_user").with(csrf())
                .flashAttr("userDto", userDto))
                .andExpect(status().is3xxRedirection());
        verify(userService, times(1)).save(userDto);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUserWithDefaultRoleTest() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .username("username")
                .password("password")
                .roles(Set.of())
                .build();
        when(userService.save(userDto)).thenReturn(userDto);
        mockMvc.perform(post("/admin/new_user").with(csrf())
                .flashAttr("userDto", userDto))
                .andExpect(status().is3xxRedirection());
        verify(userService, times(1)).save(userDto);
        verify(roleService, times(1)).findRoleByName("ROLE_STUDENT");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUserTest() throws Exception {
        final long userId = 1L;
        doNothing().when(userService).deleteById(userId);
        mockMvc.perform(delete("/admin/user/{id}", userId).with(csrf()))
                .andExpect(status().is3xxRedirection());
        verify(userService).deleteById(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void userProfileTest() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .username("username")
                .password("password")
                .roles(Set.of())
                .courses(Set.of())
                .build();
        when(userService.userById(userDto.getId())).thenReturn(userDto);
        mockMvc.perform(get("/admin/user/{id}", userDto.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("userDto", userDto));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeUserProfileTest() throws Exception {
        UserDto userDtoUpdate = UserDto.builder()
                .id(1L)
                .username("username")
                .password("password")
                .roles(Set.of())
                .courses(Set.of())
                .build();
        doNothing().when(userService).updateUsernameAndPasswordAndRoles(userDtoUpdate);
        mockMvc.perform(post("/admin/user/{id}", userDtoUpdate.getId()).with(csrf())
                .flashAttr("userDto", userDtoUpdate))
                .andExpect(status().is3xxRedirection());
        verify(userService).updateUsernameAndPasswordAndRoles(userDtoUpdate);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeUserProfileInvalidDataTest() throws Exception {
        UserDto userDtoUpdate = UserDto.builder()
                .id(1L)
                .username("")
                .password("password")
                .roles(Set.of())
                .courses(Set.of())
                .build();
        UserDto userDto = UserDto.builder()
                .id(1L)
                .username("username")
                .password("password")
                .roles(Set.of())
                .courses(Set.of())
                .build();
        when(userService.userById(userDto.getId())).thenReturn(userDto);
        mockMvc.perform(post("/admin/user/{id}", userDtoUpdate.getId()).with(csrf())
                .flashAttr("userDto", userDtoUpdate))
                .andExpect(model().hasErrors());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void takeOffUserFromCourseTest() throws Exception {
        final long userId = 1L;
        final long courseId = 1L;
        doNothing().when(userService).takeOffCourse(userId, courseId);
        mockMvc.perform(post("/admin/user/{userId}/take_off/{courseId}", userId, courseId).with(csrf()))
                .andExpect(status().is3xxRedirection());
        verify(userService, times(1)).takeOffCourse(userId, courseId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void usernameExistExceptionHandlerTest() throws Exception {
        UserDto userDtoUpdate = UserDto.builder()
                .id(1L)
                .username("username")
                .password("password")
                .roles(Set.of())
                .courses(Set.of())
                .build();
        doThrow(UsernameExistException.class).when(userService).updateUsernameAndPasswordAndRoles(userDtoUpdate);
        mockMvc.perform(post("/admin/user/{id}", userDtoUpdate.getId()).with(csrf())
                .flashAttr("userDto", userDtoUpdate))
                .andExpect(status().isOk())
                .andExpect(model().attribute("username", "exist"));
    }
}
