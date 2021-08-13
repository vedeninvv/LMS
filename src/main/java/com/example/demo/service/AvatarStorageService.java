package com.example.demo.service;

import com.example.demo.dao.AvatarImageRepository;
import com.example.demo.dao.UserRepository;
import com.example.demo.domain.AvatarImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.InputStream;

@Service
public class AvatarStorageService {

    private static final Logger logger = LoggerFactory.getLogger(AvatarStorageService.class);

    private final AvatarImageRepository avatarImageRepository;

    private final UserRepository userRepository;

    @Value("${file.storage.path}")
    private String path;

    @Autowired
    public AvatarStorageService(AvatarImageRepository avatarImageRepository, UserRepository userRepository) {
        this.avatarImageRepository = avatarImageRepository;
        this.userRepository = userRepository;
    }

//    @Transactional
//    public void save(String username, String contentType, InputStream is) {
//        Optional<AvatarImage> opt = avatarImageRepository.findByUsername(username);
//        AvatarImage avatarImage;
//        String filename;
//        if (opt.isEmpty()) {
//            filename = UUID.randomUUID().toString();
//            User user = userRepository.findUserByUsername(username)
//                    .orElseThrow(IllegalArgumentException::new);
//            avatarImage = new AvatarImage(null, contentType, filename, user);
//        } else {
//            avatarImage = opt.get();
//            filename = avatarImage.getFilename();
//            avatarImage.setContentType(contentType);
//        }
//        avatarImageRepository.save(avatarImage);
//
//        try (OutputStream os = Files.newOutputStream(Path.of(path, filename), CREATE, WRITE, TRUNCATE_EXISTING)) {
//            is.transferTo(os);
//        } catch (Exception ex) {
//            logger.error("Can't write to file {}", filename, ex);
//            throw new IllegalStateException(ex);
//        }
//    }
}