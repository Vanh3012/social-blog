package com.socialblog.repository;

import com.socialblog.model.entity.PostVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostVideoRepository extends JpaRepository<PostVideo, Long> {
}
