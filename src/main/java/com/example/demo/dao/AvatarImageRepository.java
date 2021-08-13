package com.example.demo.dao;

import com.example.demo.domain.AvatarImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AvatarImageRepository extends JpaRepository<AvatarImage, Long> {
//    Optional<AvatarImage> findByUsername(String username);
}
