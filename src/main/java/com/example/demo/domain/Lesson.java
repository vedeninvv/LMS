package com.example.demo.domain;

import lombok.*;

import javax.persistence.*;

@Data
@Entity
@Table(name = "lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @Lob
    @Column
    private String text;

    @ManyToOne(optional = false)
    private Course course;
}
