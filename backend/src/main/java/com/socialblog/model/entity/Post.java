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

    @Enumerated(EnumType.STRING)
    private Visibility visibility = Visibility.PUBLIC;

    // Thêm các trường đếm
    @Column(name = "like_count")
    private int likeCount = 0;

    @Column(name = "comment_count")
    private int commentCount = 0;

    @Builder.Default
    @Column(name = "repost_count")
    private Integer repostCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_post_id")
    private Post originalPost;

    // ===== Quan hệ với tác giả (User) =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // ===== Quan hệ với Reaction =====
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reaction> reactions = new ArrayList<>();

    // ===== Quan hệ với Comment =====
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // ===== Quan hệ với PostImage =====
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images = new ArrayList<>();

    // ===== Quan hệ với PostVideo =====
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostVideo> videos = new ArrayList<>();

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

    public int getRepostCount() {
        return this.repostCount == null ? 0 : this.repostCount;
    }

    public void incrementRepostCount() {
        if (this.repostCount == null) {
            this.repostCount = 0;
        }
        this.repostCount = this.repostCount + 1;
    }
}
