package com.socialblog.model.entity;

import com.socialblog.model.BaseEntity;
import com.socialblog.model.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "cover_url")
    private String coverUrl;

    private String bio;
    private String address;

    @Column(name = "citizen_id")
    private String citizenId;

    @Column(name = "phone")
    private String phoneNumber;

    @Column(name = "dob")
    private LocalDate dateOfBirth;

    private boolean active = true;
    private boolean verified = false;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    // ===== Relationships =====
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reaction> reactions = new ArrayList<>();

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Friendship> sentFriendRequests = new ArrayList<>();

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Friendship> receivedFriendRequests = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConversationMember> conversationMembers = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "user_following", joinColumns = @JoinColumn(name = "follower_id"), inverseJoinColumns = @JoinColumn(name = "following_id"))
    private Set<User> following = new HashSet<>();

    @ManyToMany(mappedBy = "following")
    private Set<User> followers = new HashSet<>();

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
