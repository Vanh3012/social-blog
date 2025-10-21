package com.socialblog.model.entity;

import com.socialblog.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "comments")
public class Comment extends BaseEntity {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // ===== Người viết bình luận =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // ===== Bài viết mà bình luận thuộc về =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // ===== Bình luận cha (nếu là trả lời) =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    // ===== Danh sách các phản hồi (bình luận con) =====
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> replies = new ArrayList<>();
}
