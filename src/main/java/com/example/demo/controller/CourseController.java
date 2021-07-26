package com.example.demo.controller;

import com.example.demo.domain.Course;
import com.example.demo.domain.User;
import com.example.demo.service.CourseLister;
import com.example.demo.service.LessonLister;
import com.example.demo.service.UserLister;
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
    private final UserLister userLister;
    private final LessonLister lessonLister;

    @Autowired
    public CourseController(CourseLister courseLister, UserLister userLister, LessonLister lessonLister) {
        this.courseLister = courseLister;
        this.userLister = userLister;
        this.lessonLister = lessonLister;
    }

    @GetMapping
    public String courseTable(Model model, @RequestParam(name = "titlePrefix", required = false) String titlePrefix) {
        model.addAttribute("courses", courseLister.coursesByTitleWithPrefix(titlePrefix));
        return "CoursesList";
    }

    @GetMapping("/{id}")
    public String courseForm(Model model, @PathVariable("id") Long id) {
        Course course = courseLister.courseById(id);
        model.addAttribute("course", course);
        model.addAttribute("lessons", lessonLister.lessonsByCourseWithoutText(id));
        model.addAttribute("users", userLister.usersAssignedToCourse(id));
        return "CreateCourse";
    }

    @PostMapping
    public String submitCourseForm(@Valid Course course, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "CreateCourse";
        }
        if (course.getId() != null && courseLister.hasCourseById(course.getId())){
            Course oldCourse = courseLister.courseById(course.getId());
            oldCourse.setTitle(course.getTitle());
            oldCourse.setAuthor(course.getAuthor());
            courseLister.save(oldCourse);
        }
        else {
            courseLister.save(course);
        }
        return "redirect:/course";
    }

    @GetMapping("/new")
    public String courseForm(Model model) {
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
        ModelAndView modelAndView = new ModelAndView("NotFoundException");
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        return modelAndView;
    }

    @GetMapping("/{id}/assign")
    public String assignCourse(Model model, @PathVariable Long id,
                               @RequestParam(name = "titlePrefix", required = false) String titlePrefix) {
        model.addAttribute("assignForm", "show");
        model.addAttribute("courses", courseLister.coursesByTitleWithPrefix(titlePrefix));
        model.addAttribute("courseId", id);
        model.addAttribute("users", userLister.usersNotAssignedToCourse(id));
        return "CoursesList";
    }

    @PostMapping("/{courseId}/assign")
    public String assignUserForm(@PathVariable("courseId") Long courseId,
                                 @RequestParam("userId") Long id) {
        User user = userLister.userById(id);
        Course course = courseLister.courseById(courseId);
        course.getUsers().add(user);
        user.getCourses().add(course);
        courseLister.save(course);
        return "redirect:/course";
    }

    @PostMapping("/{courseId}/take_off/{userId}")
    public String takeOffUser(@PathVariable Long courseId, @PathVariable Long userId){
        User user = userLister.userById(userId);
        Course course = courseLister.courseById(courseId);
        user.getCourses().remove(course);
        course.getUsers().remove(user);
        courseLister.save(course);
        return "redirect:/course/{courseId}";
    }
}