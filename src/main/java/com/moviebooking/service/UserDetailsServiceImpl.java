package com.moviebooking.service;

import com.moviebooking.entity.User;
import com.moviebooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Tìm user từ database bằng email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));

        // 2. Lấy tên Role từ Entity (ví dụ: "USER" hoặc "ADMIN")
        String roleName = user.getRole().getName();

        // 3. Chuyển đổi tên Role thành GrantedAuthority mà Spring Security hiểu được
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleName);

        // 4. Trả về đối tượng User của Spring Security kèm theo danh sách quyền
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(authority) // Đã thay ArrayList trống bằng quyền thực tế
        );
    }
}