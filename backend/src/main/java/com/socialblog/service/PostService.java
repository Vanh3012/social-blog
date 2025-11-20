package com.socialblog.service;

import com.socialblog.dto.PostRequest;
import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.PostImage;
import com.socialblog.model.entity.User;
import com.socialblog.model.enums.Visibility;
import com.socialblog.repository.PostImageRepository;
import com.socialblog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.transaction.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;

    private final String UPLOAD_FOLDER = "src/main/resources/static/uploads/";

    // ====================== TẠO BÀI VIẾT ======================
    public void createPost(PostRequest request, User author, List<MultipartFile> files) {

        Post post = Post.builder()
                .content(request.getContent())
                .visibility(request.getVisibility() != null ? request.getVisibility() : Visibility.PUBLIC)
                .author(author)
                .build();

        post = postRepository.save(post);

        // Lưu ảnh
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file.isEmpty())
                    continue;

                String fileName = saveFile(file);

                PostImage img = PostImage.builder()
                        .imageUrl(fileName) // chỉ lưu tên file
                        .post(post)
                        .build();

                postImageRepository.save(img);
            }
        }
    }

    // ====================== LƯU FILE ẢNH ======================
    private String saveFile(MultipartFile file) {
        try {
            File dir = new File(UPLOAD_FOLDER);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String original = file.getOriginalFilename();
            String fileName = System.currentTimeMillis() + "_" + original;

            Path path = Paths.get(UPLOAD_FOLDER + fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            return fileName;

        } catch (Exception e) {
            log.error("Lỗi lưu file: ", e);
            throw new RuntimeException("Không thể lưu file");
        }
    }

    // ====================== LẤY POST ======================
    public List<Post> getPublicPosts() {
        return postRepository.findByVisibilityOrderByCreatedAtDesc(Visibility.PUBLIC);
    }

    public List<Post> getPostsForUser(User user) {
        return postRepository.findPostsForUser(user.getId());
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));
    }

    // ====================== CẬP NHẬT BÀI VIẾT ======================
    public void updatePost(Long postId, Long userId, String content, Visibility visibility,
            List<MultipartFile> images) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Check quyền
        if (!post.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("No permission");
        }

        // Update nội dung
        post.setContent(content);
        post.setVisibility(visibility);

        // Xử lý ảnh mới
        if (images != null && images.stream().anyMatch(f -> !f.isEmpty())) {

            // Xóa ảnh cũ
            postImageRepository.deleteByPost(post);

            // Upload ảnh mới
            for (MultipartFile file : images) {
                if (!file.isEmpty()) {
                    String fileName = saveFile(file);

                    PostImage img = PostImage.builder()
                            .post(post)
                            .imageUrl(fileName)
                            .build();

                    postImageRepository.save(img);
                }
            }
        }

        postRepository.save(post);
    }

    // ====================== XÓA BÀI VIẾT ======================
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("No permission");
        }

        postRepository.delete(post);
    }

}
