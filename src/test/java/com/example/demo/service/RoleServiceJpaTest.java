package com.example.demo.service;

import com.example.demo.dao.RoleRepository;
import com.example.demo.domain.Role;
import com.example.demo.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class RoleServiceJpaTest {
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private RoleService roleService;

    @TestConfiguration
    static class testConfig {
        @Bean
        RoleService roleService(RoleRepository roleRepository) {
            return new RoleService(roleRepository);
        }
    }

    @Test
    void findRoleByNameWhenExistTest() {
        Role actualRoleStudent = roleService.findRoleByName("ROLE_STUDENT");

        assertEquals(2L, actualRoleStudent.getId());
        assertEquals("ROLE_STUDENT", actualRoleStudent.getName());
    }

    @Test
    void findRoleByNameWhenNotExistTest() {
        assertThrows(NotFoundException.class, () -> roleService.findRoleByName("ROLE_ANOTHER"));
    }

    @Test
    void findAllTest() {
        List<Role> actualRoleList = roleService.findAll();

        assertEquals(2, actualRoleList.size());
        assertTrue(actualRoleList.stream().map(Role::getName).collect(Collectors.toList())
                .containsAll(List.of("ROLE_STUDENT", "ROLE_ADMIN")));
    }
}
