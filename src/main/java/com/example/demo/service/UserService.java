package com.example.demo.service;

import com.example.demo.dao.UserRepository;
import com.example.demo.domain.Course;
import com.example.demo.domain.User;
import com.example.demo.dto.UserDto;
import com.example.demo.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final CourseService courseService;
    private final PasswordEncoder encoder;

    @Autowired
    public UserService(UserRepository userRepository, CourseService courseService, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.courseService = courseService;
        this.encoder = encoder;
    }

    public UserDto userById(Long id) {
        return toUserDto(userRepository.findById(id).orElseThrow(NotFoundException::new));
    }

    public List<UserDto> usersNotAssignedToCourse(Long courseId) {
        return userRepository.findUsersNotAssignedToCourse(courseId).stream()
                .map(this::toUserDto).collect(Collectors.toList());
    }

    public List<UserDto> usersAssignedToCourse(Long courseId) {
        return userRepository.findUsersAssignedToCourse(courseId).stream()
                .map(this::toUserDto).collect(Collectors.toList());
    }

    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(this::toUserDto).collect(Collectors.toList());
    }

    public UserDto findByUsername(String username) {
        return toUserDto(userRepository.findUserByUsername(username).orElseThrow(NotFoundException::new));
    }

    public void deleteById(long id) {
        User user = userRepository.findById(id).orElseThrow(NotFoundException::new);
        for (var course : user.getCourses()){
            takeOffCourse(id, course.getId());
        }
        userRepository.deleteById(id);
    }

    public void save(UserDto userDto) {
        userRepository.save(toUser(userDto));
    }

    public UserDto updateUsernameAndPassword(UserDto userDto){
        User user = userRepository.findById(userDto.getId()).orElseThrow(NotFoundException::new);
        user.setUsername(userDto.getUsername());
        user.setPassword(encoder.encode(userDto.getPassword()));
        userRepository.save(user);
        userDto.setPassword(encoder.encode(userDto.getPassword()));
        return userDto;
    }

    public void updateUsernameAndPasswordAndRoles(UserDto userDto){
        User user = userRepository.findById(userDto.getId()).orElseThrow(NotFoundException::new);
        user.setUsername(userDto.getUsername());
        user.setPassword(encoder.encode(userDto.getPassword()));
        user.setRoles(userDto.getRoles());
        userRepository.save(user);
    }

    public void assignCourseToUser(Long userId, Long courseId) {
        User user = userRepository.getById(userId);
        Course course = courseService.courseById(courseId);
        course.getUsers().add(user);
        user.getCourses().add(course);
        userRepository.save(user);
    }

    public void takeOffCourse(Long userId, Long courseId){
        User user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
        Course course = courseService.courseById(courseId);
        user.getCourses().remove(course);
        course.getUsers().remove(user);
        userRepository.save(user);
    }

    private UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                "",
                user.getCourses(),
                user.getRoles()
        );
    }

    private User toUser(UserDto userDto) {
        return new User(
                userDto.getId(),
                userDto.getUsername(),
                encoder.encode(userDto.getPassword()),
                userDto.getCourses(),
                userDto.getRoles()
        );
    }


}
