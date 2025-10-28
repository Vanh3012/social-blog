package com.socialblog.model.entity;

import com.socialblog.model.BaseEntity;
import com.socialblog.model.enums.Visibility;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "posts")
public class Post extends BaseEntity {

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private Visibility visibility = Visibility.PUBLIC;

    // Thêm các trường đếm
    @Column(name = "like_count")
    private int likeCount = 0;

    @Column(name = "comment_count")
    private int commentCount = 0;

    // ===== Quan hệ với tác giả (User) =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // ===== Nếu cần user_id riêng (ví dụ người đăng lại) =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // ===== Quan hệ với Reaction =====
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reaction> reactions = new ArrayList<>();

    // ===== Quan hệ với Comment =====
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // ===== Helper methods để tính toán count =====
    public int getLikeCount() {
        if (reactions != null) {
            return (int) reactions.stream()
                    .filter(r -> r.getType() != null)
                    .count();
        }
        return this.likeCount;
    }

    public int getCommentCount() {
        if (comments != null) {
            return comments.size();
        }
        return this.commentCount;
    }

    public void updateLikeCount() {
        this.likeCount = getLikeCount();
    }

    public void updateCommentCount() {
        this.commentCount = getCommentCount();
    }
}