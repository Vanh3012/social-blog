package com.socialblog.repository;

import com.socialblog.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Tìm user theo email hoặc username
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    // Kiểm tra email hoặc username đã tồn tại chưa
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    long countByActiveTrue();
}
