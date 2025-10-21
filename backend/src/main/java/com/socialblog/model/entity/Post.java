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
}
