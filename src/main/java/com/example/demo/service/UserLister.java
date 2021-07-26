package com.example.demo.service;

import com.example.demo.exceptions.NotFoundException;
import com.example.demo.dao.UserRepository;
import com.example.demo.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
public class UserLister {
    private final UserRepository userRepository;

    @Autowired
    public UserLister(UserRepository userRepository) {
        this.userRepository = userRepository;

        //creating default users just for test
        if (userRepository.findById(1L).isEmpty()){
            User user1 = new User();
            user1.setUsername("user1");
            user1.setCourses(new HashSet<>());
            userRepository.save(user1);
        }
        if (userRepository.findById(2L).isEmpty()){
            User user1 = new User();
            user1.setUsername("user2");
            user1.setCourses(new HashSet<>());
            userRepository.save(user1);
        }
        if (userRepository.findById(3L).isEmpty()){
            User user1 = new User();
            user1.setUsername("user3");
            user1.setCourses(new HashSet<>());
            userRepository.save(user1);
        }
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
