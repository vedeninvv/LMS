package com.example.demo.service;

import com.example.demo.exceptions.NotFoundException;
import com.example.demo.dao.UserRepository;
import com.example.demo.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserLister {
    private final UserRepository userRepository;

    @Autowired
    public UserLister(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User userById(Long id){
        return userRepository.findById(id).orElseThrow(NotFoundException::new);
    }

    public List<User> usersNotAssignedToCourse(Long courseId){
        return userRepository.findUsersNotAssignedToCourse(courseId);
    }

    public List<User> usersAssignedToCourse(Long courseId){
        return userRepository.findUsersAssignedToCourse(courseId);
    }
}
