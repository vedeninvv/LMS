package com.example.demo.controller;

import com.example.demo.domain.Role;
import com.example.demo.dto.UserDto;
import com.example.demo.service.RoleService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final RoleService roleService;
    private final UserService userService;

    @Autowired
    public AdminController(RoleService roleService, UserService userService) {
        this.roleService = roleService;
        this.userService = userService;
    }

    @ModelAttribute("roles")
    public List<Role> allRoles() {
        return roleService.findAll();
    }

    @GetMapping
    public String adminPanel(Model model) {
        model.addAttribute("userDto", new UserDto());
        model.addAttribute("roles", roleService.findAll());
        model.addAttribute("allUsers", userService.findAll());
        return "AdminPanel";
    }

    @PostMapping("/new_user")
    public String createUser(@Valid UserDto userDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "AdminPanel";
        }
        if (userDto.getRoles() == null || userDto.getRoles().isEmpty()){
            Set<Role> defaultRoles = new HashSet<>();
            defaultRoles.add(roleService.findRoleByName("ROLE_STUDENT"));
            userDto.setRoles(defaultRoles);
        }
        userService.save(userDto);
        return "redirect:/admin";
    }

    @DeleteMapping("/user/{id}")
    public String deleteUser(@PathVariable Long id){
        userService.deleteById(id);
        return "redirect:/admin";
    }

    @GetMapping("/user/{id}")
    public String userProfile(@PathVariable Long id, Model model){
        UserDto userDto = userService.userById(id);
        model.addAttribute("userDto", userDto);
        model.addAttribute("courses", userDto.getCourses());
        return "Profile";
    }

    @PostMapping("user/{id}")
    public String changeUserProfile(@PathVariable Long id, @Valid @ModelAttribute(name = "userDto") UserDto userDtoUpdate,
                                    BindingResult bindingResult, Model model){
        if (bindingResult.hasErrors()){
            UserDto userDto = userService.userById(id);
            model.addAttribute("courses", userDto.getCourses());
            return "Profile";
        }
        userService.updateUsernameAndPasswordAndRoles(userDtoUpdate);
        return "redirect:/admin/user/{id}";
    }

    @PostMapping("/user/{userId}/take_off/{courseId}")
    public String takeOffUserFromCourse(@PathVariable Long userId, @PathVariable Long courseId){
        userService.takeOffCourse(userId, courseId);
        return "redirect:/admin/user/{userId}";
    }
}
