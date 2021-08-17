package com.example.demo.service;

import com.example.demo.dao.AvatarImageRepository;
import com.example.demo.dao.CourseCoverRepository;
import com.example.demo.dao.CourseRepository;
import com.example.demo.dao.UserRepository;
import com.example.demo.domain.AvatarImage;
import com.example.demo.domain.Course;
import com.example.demo.domain.CourseCover;
import com.example.demo.domain.User;
import com.example.demo.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static java.nio.file.StandardOpenOption.*;

@Service
public class CourseCoverService {
    private static final Logger logger = LoggerFactory.getLogger(AvatarStorageService.class);

    private final CourseCoverRepository courseCoverRepository;
    private final CourseRepository courseRepository;

    @Value("${file.storage.path}")
    private String path;

    @Autowired
    public CourseCoverService(CourseCoverRepository courseCoverRepository, CourseRepository courseRepository) {
        this.courseCoverRepository = courseCoverRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public void save(Long courseId, String contentType, InputStream is) {
        Course course = courseRepository.findById(courseId).orElseThrow(NotFoundException::new);
        Optional<CourseCover> opt = courseCoverRepository.findByCourse(course);
        CourseCover courseCover;
        String filename;
        if (opt.isEmpty()) {
            filename = UUID.randomUUID().toString();
            courseCover = new CourseCover(null, contentType, filename, course);
        } else {
            courseCover = opt.get();
            filename = courseCover.getFilename();
            courseCover.setContentType(contentType);
        }
        courseCoverRepository.save(courseCover);

        try (OutputStream os = Files.newOutputStream(Path.of(path, filename), CREATE, WRITE, TRUNCATE_EXISTING)) {
            is.transferTo(os);
        } catch (Exception ex) {
            logger.error("Can't write to file {}", filename, ex);
            throw new IllegalStateException(ex);
        }
    }

    public Optional<String> getContentTypeByCourseId(Long courseID) {
        Course course = courseRepository.findById(courseID).orElseThrow(NotFoundException::new);
        return courseCoverRepository.findByCourse(course)
                .map(CourseCover::getContentType);
    }

    public Optional<byte[]> getCourseCoverImageByCourseId(Long courseID) {
        Course course = courseRepository.findById(courseID).orElseThrow(NotFoundException::new);
        return courseCoverRepository.findByCourse(course)
                .map(CourseCover::getFilename)
                .map(filename -> {
                    try {
                        return Files.readAllBytes(Path.of(path, filename));
                    } catch (IOException ex) {
                        logger.error("Can't read file {}", filename, ex);
                        throw new IllegalStateException(ex);
                    }
                });
    }
}
