package com.socialblog.service;

import com.socialblog.dto.PostRequest;
import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.User;
import com.socialblog.repository.FriendshipRepository;
import com.socialblog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final FriendshipRepository friendshipRepository;

    @Transactional
    public Post createPost(PostRequest request, User author) {
        Post post = Post.builder()
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .visibility(request.getVisibility())
                .author(author)
                .build();

        Post savedPost = postRepository.save(post);
        log.info("User {} created post {}", author.getUsername(), savedPost.getId());
        return savedPost;
    }

    @Transactional(readOnly = true)
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));
    }

    @Transactional(readOnly = true)
    public List<Post> getVisiblePosts(User currentUser) {
        // Lấy danh sách ID bạn bè
        List<Long> friendIds = friendshipRepository.findAcceptedFriends(currentUser.getId())
                .stream()
                .map(f -> f.getSender().getId().equals(currentUser.getId())
                        ? f.getReceiver().getId()
                        : f.getSender().getId())
                .collect(Collectors.toList());

        return postRepository.findVisiblePosts(currentUser.getId(), friendIds);
    }

    @Transactional(readOnly = true)
    public List<Post> getAllPublicPosts() {
        return postRepository.findByVisibilityOrderByCreatedAtDesc(
                com.socialblog.model.enums.Visibility.PUBLIC);
    }

    @Transactional(readOnly = true)
    public List<Post> getUserPosts(User user) {
        return postRepository.findByAuthorOrderByCreatedAtDesc(user);
    }

    @Transactional
    public void deletePost(Long postId, User user) {
        Post post = getPostById(postId);

        if (!post.getAuthor().getId().equals(user.getId()) && !user.isAdmin()) {
            throw new RuntimeException("Bạn không có quyền xóa bài viết này");
        }

        postRepository.delete(post);
        log.info("Post {} deleted by user {}", postId, user.getUsername());
    }

    @Transactional
    public Post updatePost(Long postId, PostRequest request, User user) {
        Post post = getPostById(postId);

        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa bài viết này");
        }

        post.setContent(request.getContent());
        post.setImageUrl(request.getImageUrl());
        post.setVisibility(request.getVisibility());

        return postRepository.save(post);
    }
}