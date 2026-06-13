package com.moviebooking.service;

import com.moviebooking.dto.AuthRequest;
import com.moviebooking.dto.AuthResponse;
import com.moviebooking.dto.UserDTO;
import com.moviebooking.entity.Role;
import com.moviebooking.entity.User;
import com.moviebooking.exception.NotFoundException;
import com.moviebooking.repository.RoleRepository;
import com.moviebooking.repository.UserRepository;
import com.moviebooking.config.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    // REGISTER - Đã cập nhật để lưu thêm số điện thoại
    public AuthResponse register(UserDTO userDTO) {

        // 1. Kiểm tra email đã tồn tại chưa
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // 2. Chuyển đổi DTO sang Entity
        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone()); // <-- THÊM DÒNG NÀY ĐỂ LƯU SỐ ĐIỆN THOẠI
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        // 3. Thiết lập Role mặc định cho người dùng mới là "USER"
        Role role = roleRepository.findByName("USER")
                .orElseThrow(() -> new NotFoundException("Role not found"));

        user.setRole(role);

        // 4. Lưu người dùng vào Database
        User savedUser = userRepository.save(user);

        // 5. Tạo JWT Token sau khi đăng ký thành công
        String token = jwtUtils.generateToken(savedUser.getEmail());

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                token,
                role.getName()
        );
    }

    // LOGIN
    public AuthResponse login(AuthRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtils.generateToken(user.getEmail());

        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                token,
                user.getRole().getName()
        );
    }

    // GET CURRENT USER
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}