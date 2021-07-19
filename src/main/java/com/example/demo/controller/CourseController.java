package com.example.demo.controller;

import com.example.demo.domain.Course;
import com.example.demo.service.CourseLister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

@Controller
@RequestMapping("/course")
public class CourseController {

    private final CourseLister courseLister;

    @Autowired
    public CourseController(CourseLister courseLister) {
        this.courseLister = courseLister;
    }

    @GetMapping
    public String courseTable(Model model, @RequestParam(name = "titlePrefix", required = false) String titlePrefix) {
        model.addAttribute("activePage", "courses");
        model.addAttribute("courses", courseLister.coursesByTitleWithPrefix(titlePrefix));
        return "CoursesList";
    }

    @GetMapping("/{id}")
    public String courseForm(Model model, @PathVariable("id") Long id) {
        model.addAttribute("activePage", "courses");
        model.addAttribute("course", courseLister.courseById(id));
        return "CreateCourse";
    }

    @PostMapping
    public String submitCourseForm(@Valid Course course, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "CreateCourse";
        }
        courseLister.save(course);
        return "redirect:/course";
    }

    @GetMapping("/new")
    public String courseForm(Model model) {
        model.addAttribute("activePage", "courses");
        model.addAttribute("course", new Course());
        return "CreateCourse";
    }

    @DeleteMapping("/{id}")
    public String deleteCourse(@PathVariable("id") Long id) {
        courseLister.delete(id);
        return "redirect:/course";
    }

    @ExceptionHandler
    public ModelAndView notFoundExceptionHandler(NotFoundException ex) {
        ModelAndView modelAndView = new ModelAndView("FindCourseException");
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        return modelAndView;
    }
}