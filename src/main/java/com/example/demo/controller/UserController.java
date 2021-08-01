package com.example.demo.controller;

import com.example.demo.domain.Role;
import com.example.demo.dto.UserDto;
import com.example.demo.exceptions.NotFoundException;
import com.example.demo.service.RoleService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

@Controller
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public UserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @ModelAttribute("activePage")
    public String activePage() {
        return "users";
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public String userProfile(Model model, Principal principal) {
        UserDto userDto = userService.findByUsername(principal.getName());
        model.addAttribute("userDto", userDto);
        model.addAttribute("courses", userDto.getCourses());
        return "Profile";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public String changeProfile(@Valid @ModelAttribute(name = "userDto") UserDto userDtoUpdate, BindingResult bindingResult,
                                Model model, HttpServletRequest request) throws ServletException {
        UserDto userDtoOld = userService.findByUsername(request.getUserPrincipal().getName());
        if (!userDtoOld.getId().equals(userDtoUpdate.getId())) {
            model.addAttribute("courses", userDtoOld.getCourses());
            return "Profile";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("courses", userDtoOld.getCourses());
            return "Profile";
        }
        userDtoUpdate = userService.updateUsernameAndPassword(userDtoUpdate);
        request.logout();
        request.login(userDtoUpdate.getUsername(), userDtoUpdate.getPassword());
        return "redirect:/user";
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping("/new")
    public String newUserForm(Model model) {
        model.addAttribute("userDto", new UserDto());
        return "RegistrationForm";
    }

    @PreAuthorize("isAnonymous()")
    @PostMapping("/new")
    public String newUser(@Valid UserDto userDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "RegistrationForm";
        }
        Set<Role> roles = new HashSet<>();
        roles.add(roleService.findRoleByName("ROLE_STUDENT"));
        userDto.setRoles(roles);
        userService.save(userDto);
        return "redirect:/user";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/leave/{courseId}")
    public String leaveCourse(@PathVariable Long courseId, Principal principal) {
        UserDto userDto = userService.findByUsername(principal.getName());
        userService.takeOffCourse(userDto.getId(), courseId);
        return "redirect:/user";
    }

    @ExceptionHandler
    public ModelAndView notFoundExceptionHandler(NotFoundException ex) {
        ModelAndView modelAndView = new ModelAndView("NotFoundException");
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        return modelAndView;
    }
}
