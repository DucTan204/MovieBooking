package com.moviebooking.service;

import com.moviebooking.dto.UserDTO;
import com.moviebooking.entity.User;
import com.moviebooking.exception.NotFoundException;
import com.moviebooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder; // Quan trọng cho bảo mật
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ✅ Chuyển đổi Entity -> DTO (Giữ lại Role để không mất quyền)
    public UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());

        if (user.getRole() != null) {
            dto.setRoleName(user.getRole().getName());
        }

        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    // --- DÀNH CHO ADMIN ---

    public List<UserDTO> findAll() {
        return userRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public UserDTO findById(Long id) {
        return toDTO(userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id)));
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    // --- DÀNH CHO NGƯỜI DÙNG (PROFILE) ---

    @Transactional
    public UserDTO updateProfile(Long id, UserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Chỉ cập nhật nếu giá trị mới không trống (Tránh ghi đè rỗng)
        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName());
        }

        // Cập nhật số điện thoại (Cho phép null nếu muốn xóa số điện thoại)
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }

        // TUYỆT ĐỐI KHÔNG cập nhật Email, Password, Role ở hàm này để bảo mật
        return toDTO(userRepository.save(user));
    }

    @Transactional
    public void changePassword(Long id, String oldPass, String newPass) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Kiểm tra mật khẩu cũ có khớp với DB không
        if (!passwordEncoder.matches(oldPass, user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không chính xác!");
        }

        // Mã hóa mật khẩu mới trước khi lưu
        user.setPassword(passwordEncoder.encode(newPass));
        userRepository.save(user);
    }

    // --- HÀM CẬP NHẬT TỔNG THỂ (Dành cho Admin sửa User khác) ---
    @Transactional
    public UserDTO updateByAdmin(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (userDTO.getName() != null) user.setName(userDTO.getName());
        if (userDTO.getEmail() != null) user.setEmail(userDTO.getEmail());
        if (userDTO.getPhone() != null) user.setPhone(userDTO.getPhone());

        // Lưu ý: Nếu Admin muốn đổi Role, bạn nên nhận RoleId từ DTO
        // và tìm RoleEntity từ RoleRepository để gán vào user.setRole(...)

        return toDTO(userRepository.save(user));
    }
}