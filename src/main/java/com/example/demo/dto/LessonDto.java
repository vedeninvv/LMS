package com.example.demo.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonDto {

    private Long id;

    @NotBlank(message = "Lesson title has to be filled")
    private String title;

    @NotBlank(message = "Lesson text has to be filled")
    private String text;

    private Long courseId;

    public LessonDto(Long courseId) {
        this.courseId = courseId;
    }

    public LessonDto(Long id, String title, Long courseId) {
        this.id = id;
        this.title = title;
        this.courseId = courseId;
    }
}
