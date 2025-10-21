package com.socialblog.service;

import com.socialblog.model.entity.User;
import com.socialblog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

        private final UserRepository userRepository;

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                // Tìm user theo username
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user: " + username));

                // Kiểm tra tài khoản có active không
                if (!user.isActive()) {
                        throw new UsernameNotFoundException("Tài khoản đã bị khóa!");
                }

                // Tạo authorities
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

                // Trả về UserDetails
                return org.springframework.security.core.userdetails.User.builder()
                                .username(user.getUsername())
                                .password(user.getPassword())
                                .authorities(authorities)
                                .accountExpired(false)
                                .accountLocked(!user.isActive())
                                .credentialsExpired(false)
                                .disabled(!user.isActive())
                                .build();
        }
}