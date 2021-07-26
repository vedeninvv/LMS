package com.example.demo.controller;

import com.example.demo.dto.LessonDto;
import com.example.demo.exceptions.NotFoundException;
import com.example.demo.service.LessonLister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

@Controller
@RequestMapping("/course/{courseId}/lessons")
public class LessonController {
    private final LessonLister lessonLister;

    @Autowired
    public LessonController(LessonLister lessonLister) {
        this.lessonLister = lessonLister;
    }

    @GetMapping("/new")
    public String newLesson(Model model, @PathVariable Long courseId) {
        model.addAttribute("lessonDto", new LessonDto(courseId));
        return "CreateLesson";
    }

    @GetMapping("/{lessonId}")
    public String lessonForm(Model model, @PathVariable Long courseId, @PathVariable Long lessonId) {
        model.addAttribute("lessonDto", lessonLister.lessonById(lessonId));
        return "CreateLesson";
    }

    @PostMapping
    public String submitLessonForm(@Valid LessonDto lessonDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "CreateLesson";
        }
        lessonLister.save(lessonDto);
        return "redirect:/course/{courseId}";
    }

    @DeleteMapping("/{lessonId}")
    public String deleteLesson(@PathVariable Long courseId, @PathVariable Long lessonId){
        lessonLister.deleteLessonById(lessonId);
        return "redirect:/course/{courseId}";
    }

    @ExceptionHandler
    public ModelAndView notFoundExceptionHandler(NotFoundException ex) {
        ModelAndView modelAndView = new ModelAndView("NotFoundException");
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        return modelAndView;
    }
}
