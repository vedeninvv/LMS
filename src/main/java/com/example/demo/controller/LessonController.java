package com.example.demo.controller;

import com.example.demo.dto.LessonDto;
import com.example.demo.exceptions.NotFoundException;
import com.example.demo.service.LessonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

@Controller
@RequestMapping("/course/{courseId}/lessons")
public class LessonController {
    private final LessonService lessonService;

    @Autowired
    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @ModelAttribute("activePage")
    public String activePage() {
        return "courses";
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/new")
    public String newLesson(Model model, @PathVariable Long courseId) {
        model.addAttribute("lessonDto", new LessonDto(courseId));
        return "CreateLesson";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{lessonId}")
    public String lessonForm(Model model, @PathVariable Long courseId, @PathVariable Long lessonId) {
        model.addAttribute("lessonDto", lessonService.lessonById(lessonId));
        return "CreateLesson";
    }

    @Secured("ROLE_ADMIN")
    @PostMapping
    public String submitLessonForm(@Valid LessonDto lessonDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "CreateLesson";
        }
        lessonService.save(lessonDto);
        return "redirect:/course/{courseId}";
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{lessonId}")
    public String deleteLesson(@PathVariable Long courseId, @PathVariable Long lessonId){
        lessonService.deleteLessonById(lessonId);
        return "redirect:/course/{courseId}";
    }

    @ExceptionHandler
    public ModelAndView notFoundExceptionHandler(NotFoundException ex) {
        ModelAndView modelAndView = new ModelAndView("NotFoundException");
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        return modelAndView;
    }
}
