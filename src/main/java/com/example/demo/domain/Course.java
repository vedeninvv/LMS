package com.example.demo.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Course {
    private Long id;

    @NotBlank(message = "Course author has to be filled")
    private String author;

    @NotBlank(message = "Course title has to be filled")
    private String title;
}