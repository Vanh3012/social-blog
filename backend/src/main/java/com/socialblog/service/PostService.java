package com.socialblog.service;

import com.socialblog.dto.PostRequest;
import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.PostImage;
import com.socialblog.model.entity.PostVideo;
import com.socialblog.model.entity.User;
import com.socialblog.model.enums.Visibility;
import com.socialblog.repository.PostImageRepository;
import com.socialblog.repository.PostRepository;
import com.socialblog.repository.PostVideoRepository;
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
    private final PostVideoRepository postVideoRepository;

    private final String UPLOAD_FOLDER = "src/main/resources/static/uploads/";
    private final String UPLOAD_VIDEO_FOLDER = "src/main/resources/static/uploads_video/";

    // ====================== TẠO BÀI VIẾT ======================
    public void createPost(PostRequest request, User author, List<MultipartFile> images, List<MultipartFile> videos) {

        Post post = Post.builder()
                .content(request.getContent())
                .visibility(request.getVisibility() != null ? request.getVisibility() : Visibility.PUBLIC)
                .author(author)
                .build();

        post = postRepository.save(post);

        // Lưu ảnh
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                if (file.isEmpty())
                    continue;

                String fileName = saveFile(file);

                PostImage img = PostImage.builder()
                        .imageUrl(fileName)
                        .post(post)
                        .build();

                postImageRepository.save(img);
            }
        }

        // Lưu video
        if (videos != null && !videos.isEmpty()) {
            for (MultipartFile file : videos) {
                if (file.isEmpty())
                    continue;

                String fileName = saveVideo(file);

                PostVideo video = PostVideo.builder()
                        .videoUrl(fileName)
                        .post(post)
                        .build();

                postVideoRepository.save(video);
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

    // ====================== LƯU FILE VIDEO ======================
    private String saveVideo(MultipartFile file) {
        try {
            File dir = new File(UPLOAD_VIDEO_FOLDER);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String original = file.getOriginalFilename();
            String fileName = System.currentTimeMillis() + "_" + original;

            Path path = Paths.get(UPLOAD_VIDEO_FOLDER + fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            return fileName;

        } catch (Exception e) {
            log.error("Lỗi lưu video: ", e);
            throw new RuntimeException("Không thể lưu video");
        }
    }

    // ====================== LẤY POST ======================
    public List<Post> getPublicPosts() {
        return postRepository.findByVisibilityOrderByCreatedAtDesc(Visibility.PUBLIC);
    }

    public List<Post> getPostsForUser(User user, List<Long> friendIds) {
        if (user == null) {
            return getPublicPosts();
        }
        List<Long> ids = (friendIds == null || friendIds.isEmpty()) ? List.of(-1L) : friendIds;
        return postRepository.findVisibleForUser(user.getId(), ids);
    }

    public List<Post> getPostsForUser(User user) {
        return getPostsForUser(user, List.of());
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));
    }

    // ====================== REPOST ======================
    public Post repost(Post originalPost, User author, String note) {
        if (originalPost == null) {
            throw new RuntimeException("Bài viết không tồn tại");
        }

        if (originalPost.getVisibility() == Visibility.PRIVATE
                && !originalPost.getAuthor().getId().equals(author.getId())) {
            throw new RuntimeException("Bài viết riêng tư không thể chia sẻ");
        }

        Post repost = Post.builder()
                .content(note)
                .visibility(originalPost.getVisibility())
                .author(author)
                .originalPost(originalPost)
                .build();

        originalPost.incrementRepostCount();
        postRepository.save(originalPost);

        return postRepository.save(repost);
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
