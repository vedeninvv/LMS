package com.example.demo.controller;

import com.example.demo.dto.UserDto;
import com.example.demo.exceptions.NotFoundException;
import com.example.demo.exceptions.UsernameExistException;
import com.example.demo.service.AvatarStorageService;
import com.example.demo.service.RoleService;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @MockBean
    private UserService userService;
    @MockBean
    private RoleService roleService;
    @MockBean
    private AvatarStorageService avatarStorageService;
    @Autowired
    private MockMvc mockMvc;

    private List<UserDto> userList = new ArrayList<>();

    @BeforeEach
    void setUp() {
        UserDto userDtoArtur = UserDto.builder()
                .id(1L)
                .username("usernameArtur")
                .password("passwordArtur")
                .courses(new HashSet<>())
                .roles(new HashSet<>())
                .build();
        userList.add(userDtoArtur);
        Mockito.when(userService.findByUsername("usernameArtur")).thenReturn(userDtoArtur);
    }

    @Test
    @WithMockUser(username = "usernameArtur")
    void userProfileTest() throws Exception {
        UserDto userDto = userList.get(0);
        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("userDto", "courses", "activePage"))
                .andExpect(model().attribute("userDto", userDto));
    }

    @Test
    @WithMockUser(username = "usernameArtur")
    void changeProfileTest() throws Exception {
        UserDto userDto = userList.get(0);
        UserDto newUserDto = UserDto.builder()
                .id(1L)
                .username("newUsernameArtur")
                .password("newPasswordArtur")
                .courses(new HashSet<>())
                .roles(new HashSet<>())
                .build();
        when(userService.updateUsernameAndPassword(userDto)).thenReturn(newUserDto);
        mockMvc.perform(post("/user").with(csrf())
                .flashAttr("userDto", newUserDto))
                .andExpect(status().is3xxRedirection());
        verify(userService, times(1)).updateUsernameAndPassword(any());
    }

    @Test
    @WithMockUser(username = "usernameArtur")
    void changeProfileInvalidDataTest() throws Exception {
        UserDto newUserDto = UserDto.builder()
                .id(1L)
                .username("")
                .password("newPasswordArtur")
                .courses(new HashSet<>())
                .roles(new HashSet<>())
                .build();
        mockMvc.perform(post("/user").with(csrf())
                .flashAttr("userDto", newUserDto))
                .andExpect(model().hasErrors());
    }

    @Test
    void newUserFormTest() throws Exception {
        mockMvc.perform(get("/user/new"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("userDto", "username", "activePage"))
                .andExpect(view().name("RegistrationForm"));
    }

    @Test
    void newUserTest() throws Exception {
        UserDto userDto = userList.get(0);
        when(userService.save(any())).thenReturn(null);
        mockMvc.perform(post("/user/new").with(csrf())
                .flashAttr("userDto", userDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user"));
        verify(userService, times(1)).save(any());
    }

    @Test
    void newUserInvalidDataTest() throws Exception {
        UserDto userDto = userList.get(0);
        userDto.setUsername("");
        mockMvc.perform(post("/user/new").with(csrf())
                .flashAttr("userDto", userDto))
                .andExpect(model().hasErrors());
    }

    @Test
    @WithMockUser(username = "usernameArtur")
    void leaveCourseTest() throws Exception {
        UserDto userDto = userList.get(0);
        doNothing().when(userService).takeOffCourse(userDto.getId(), 1L);
        mockMvc.perform(post("/user/leave/{courseId}", 1L).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user"));
        verify(userService, times(1)).takeOffCourse(userDto.getId(), 1L);
    }

    @Test
    @WithMockUser(username = "notFound")
    void notFoundExceptionHandlerTest() throws Exception {
        UserDto newUserDto = UserDto.builder()
                .id(1L)
                .username("newUsernameArtur")
                .password("newPasswordArtur")
                .courses(new HashSet<>())
                .roles(new HashSet<>())
                .build();
        Mockito.when(userService.findByUsername("notFound")).thenThrow(NotFoundException.class);

        mockMvc.perform(post("/user").with(csrf())
                .flashAttr("userDto", newUserDto))
                .andExpect(status().isNotFound())
                .andExpect(view().name("NotFoundException"));
    }

    @Test
    void usernameExistExceptionHandlerTest() throws Exception {
        UserDto userDto = userList.get(0);
        when(userService.save(userDto)).thenThrow(UsernameExistException.class);
        mockMvc.perform(post("/user/new").with(csrf())
                .flashAttr("userDto", userDto))
                .andExpect(model().attribute("username", "exist"));
    }
}
