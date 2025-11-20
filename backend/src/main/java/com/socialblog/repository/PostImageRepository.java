package com.socialblog.repository;

import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    List<PostImage> findByPost(Post post);

    void deleteByPost(Post post);

    @Query("SELECT pi FROM PostImage pi WHERE pi.post.author.id = :userId ORDER BY pi.id DESC")
    List<PostImage> findAllImagesByUserId(@Param("userId") Long userId);

}
