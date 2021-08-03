package com.example.demo.service;

import com.example.demo.dao.RoleRepository;
import com.example.demo.domain.Role;
import com.example.demo.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
        roleRepository.save(new Role(1, "ROLE_ADMIN", new HashSet<>()));
        roleRepository.save(new Role(2, "ROLE_STUDENT", new HashSet<>()));
    }

    public Role findRoleByName(String name){
        return roleRepository.findByName(name).orElseThrow(NotFoundException::new);
    }

    public List<Role> findAll(){
        return roleRepository.findAll();
    }
}
