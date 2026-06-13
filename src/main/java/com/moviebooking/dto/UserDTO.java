package com.moviebooking.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String password; // Thường dùng cho đăng ký
    private String phone;
    private String roleName;
    private LocalDateTime createdAt;
}