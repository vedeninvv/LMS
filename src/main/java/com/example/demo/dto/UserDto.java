package com.example.demo.dto;

import com.example.demo.domain.Course;
import com.example.demo.domain.Role;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserDto {

    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private Set<Course> courses;

    private Set<Role> roles;
}
