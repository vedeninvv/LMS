package com.example.demo.controller;

import com.example.demo.domain.Course;
import com.example.demo.dto.UserDto;
import com.example.demo.exceptions.InternalServerError;
import com.example.demo.exceptions.NotFoundException;
import com.example.demo.service.CourseCoverService;
import com.example.demo.service.CourseService;
import com.example.demo.service.LessonService;
import com.example.demo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/course")
public class CourseController {
    private final CourseService courseService;
    private final UserService userService;
    private final LessonService lessonService;
    private final CourseCoverService courseCoverService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public CourseController(CourseService courseService, UserService userService,
                            LessonService lessonService, CourseCoverService courseCoverService) {
        this.courseService = courseService;
        this.userService = userService;
        this.lessonService = lessonService;
        this.courseCoverService = courseCoverService;
    }

    @ModelAttribute("activePage")
    public String activePage() {
        return "courses";
    }

    @GetMapping
    public String courseTable(Model model, @RequestParam(name = "titlePrefix", required = false) String titlePrefix) {
        model.addAttribute("courses", courseService.coursesByTitleWithPrefix(titlePrefix));
        return "CoursesList";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public String courseForm(Model model, @PathVariable("id") Long id) {
        Course course = courseService.courseById(id);
        model.addAttribute("course", course);
        model.addAttribute("lessons", lessonService.lessonsByCourseWithoutText(id));
        model.addAttribute("users", userService.usersAssignedToCourse(id));
        return "CreateCourse";
    }

    @Secured("ROLE_ADMIN")
    @PostMapping
    public String submitCourseForm(@Valid Course course, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "CreateCourse";
        }
        if (course.getId() != null && courseService.hasCourseById(course.getId())) {
            Course oldCourse = courseService.courseById(course.getId());
            oldCourse.setTitle(course.getTitle());
            oldCourse.setAuthor(course.getAuthor());
            courseService.save(oldCourse);
        } else {
            courseService.save(course);
        }
        return "redirect:/course";
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/new")
    public String courseForm(Model model) {
        model.addAttribute("course", new Course());
        return "CreateCourse";
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{id}")
    public String deleteCourse(@PathVariable("id") Long id) {
        courseService.delete(id);
        return "redirect:/course";
    }

    @ExceptionHandler
    public ModelAndView notFoundExceptionHandler(NotFoundException ex) {
        ModelAndView modelAndView = new ModelAndView("NotFoundException");
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        return modelAndView;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/assign")
    public String assignCourse(Model model, @PathVariable Long id, HttpServletRequest request,
                               @RequestParam(name = "titlePrefix", required = false) String titlePrefix) {
        model.addAttribute("assignForm", "show");
        model.addAttribute("courses", courseService.coursesByTitleWithPrefix(titlePrefix));
        model.addAttribute("courseId", id);
        if (request.isUserInRole("ROLE_ADMIN")) {
            model.addAttribute("users", userService.usersNotAssignedToCourse(id));
        } else {
            UserDto currentUser = userService.findByUsername(request.getUserPrincipal().getName());
            if (currentUser.getCourses().stream()
                    .map(Course::getId).collect(Collectors.toList())
                    .contains(id)) {
                model.addAttribute("users", new ArrayList<UserDto>());
            } else {
                List<UserDto> listWithCurrentUser = new ArrayList<>();
                listWithCurrentUser.add(currentUser);
                model.addAttribute("users", listWithCurrentUser);
            }
        }
        return "CoursesList";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{courseId}/assign")
    public String assignUserForm(@PathVariable("courseId") Long courseId,
                                 @RequestParam("userId") Long userId, HttpServletRequest request) {
        if (request.isUserInRole("ROLE_ADMIN") ||
                userService.findByUsername(request.getUserPrincipal().getName()).getId().equals(userId)) {
            userService.assignCourseToUser(userId, courseId);
        }
        return "redirect:/course";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{courseId}/take_off/{userId}")
    public String takeOffUser(@PathVariable Long courseId, @PathVariable Long userId, HttpServletRequest request) {
        if (request.isUserInRole("ROLE_ADMIN") ||
                userService.findByUsername(request.getUserPrincipal().getName()).getId().equals(userId)) {
            userService.takeOffCourse(userId, courseId);
        }
        return "redirect:/course/{courseId}";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/cover")
    public String updateAvatarImage(@RequestParam("cover") MultipartFile cover, @PathVariable Long id) {
        logger.info("File name {}, file content type {}, file size {}", cover.getOriginalFilename(),
                cover.getContentType(), cover.getSize());
        try {
            courseCoverService.save(id, cover.getContentType(), cover.getInputStream());
        } catch (Exception ex) {
            logger.info("", ex);
            throw new InternalServerError();
        }
        return "redirect:/course/{id}";
    }

    @GetMapping("/{id}/cover")
    @ResponseBody
    public ResponseEntity<byte[]> coverImage(@PathVariable Long id) {
        String contentType = courseCoverService.getContentTypeByCourseId(id)
                .orElseThrow(NotFoundException::new);
        byte[] data = courseCoverService.getCourseCoverImageByCourseId(id)
                .orElseThrow(NotFoundException::new);
        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(data);
    }
}