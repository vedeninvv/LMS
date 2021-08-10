package com.example.demo.controller;

import com.example.demo.dto.LessonDto;
import com.example.demo.exceptions.NotFoundException;
import com.example.demo.service.LessonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LessonController.class)
public class LessonControllerTest {
    @MockBean
    private LessonService lessonService;
    @Autowired
    MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void newLessonTest() throws Exception {
        mockMvc.perform(get("/course/{courseId}/lessons/new", 1L).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("activePage", "lessonDto"));
    }

    @Test
    void lessonFormTest() throws Exception {
        LessonDto lessonDto = LessonDto.builder()
                .title("lesson")
                .text("test")
                .id(1L)
                .courseId(1L)
                .build();
        when(lessonService.lessonById(lessonDto.getId())).thenReturn(lessonDto);
        mockMvc.perform(get("/course/{courseId}/lessons/{lessonId}", 1L, 1L).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("activePage", "lessonDto"))
                .andExpect(model().attribute("lessonDto", lessonDto));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void submitLessonFormTest() throws Exception {
        LessonDto lessonDto = LessonDto.builder()
                .title("lesson")
                .text("test")
                .id(1L)
                .courseId(1L)
                .build();
        when(lessonService.save(lessonDto)).thenReturn(lessonDto);
        mockMvc.perform(post("/course/{courseId}/lessons/", 1L).with(csrf())
                .flashAttr("lessonDto", lessonDto))
                .andExpect(status().is3xxRedirection());
        verify(lessonService, times(1)).save(lessonDto);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void submitLessonFormInvalidDataTest() throws Exception {
        LessonDto lessonDto = LessonDto.builder()
                .title("")
                .text("test")
                .id(1L)
                .courseId(1L)
                .build();
        mockMvc.perform(post("/course/{courseId}/lessons/", 1L).with(csrf())
                .flashAttr("lessonDto", lessonDto))
                .andExpect(model().hasErrors());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteLessonTest() throws Exception {
        doNothing().when(lessonService).deleteLessonById(1L);
        mockMvc.perform(delete("/course/{courseId}/lessons/{lessonId}", 1L, 1L).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/course/1"));
        verify(lessonService, times(1)).deleteLessonById(1L);
    }

    @Test
    void notFoundExceptionHandlerTest() throws Exception {
        when(lessonService.lessonById(1L)).thenThrow(NotFoundException.class);
        mockMvc.perform(get("/course/{courseId}/lessons/{lessonId}", 1L, 1L).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(view().name("NotFoundException"));
    }
}
