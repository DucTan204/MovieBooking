package com.moviebooking.controller;

import com.moviebooking.dto.UserDTO;
import com.moviebooking.entity.User;
import com.moviebooking.repository.UserRepository; // Thêm import này
import com.moviebooking.service.AuthService;
import com.moviebooking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final UserRepository userRepository; // Inject qua constructor nhờ @RequiredArgsConstructor

    // ✅ 1. Lấy profile cá nhân
    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getMyProfile() {
        User currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(userService.toDTO(currentUser));
    }

    // ✅ 2. Cập nhật profile cá nhân
    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateMyProfile(@RequestBody UserDTO dto) {
        User currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(userService.updateProfile(currentUser.getId(), dto));
    }

    // ✅ 3. Đổi mật khẩu
    @PostMapping("/profile/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        User currentUser = authService.getCurrentUser();
        userService.changePassword(
                currentUser.getId(),
                request.get("oldPassword"),
                request.get("newPassword")
        );
        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }

    // ─── THÊM MỚI (Tính năng 4: Pass vé) ──────────────────────────
    // Tìm user theo email để lấy ID cho việc chuyển nhượng vé
    @GetMapping("/by-email")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> findByEmail(@RequestParam String email) {
        return userRepository.findByEmail(email)
                .map(u -> ResponseEntity.ok(Map.of(
                        "id",    u.getId(),
                        "name",  u.getName(),
                        "email", u.getEmail()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    // ---------------------------------------------------------
    // CÁC API CỦA ADMIN
    // ---------------------------------------------------------

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateByAdmin(id, userDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok("User deleted successfully");
    }
}