package com.moviebooking.service;

import com.moviebooking.entity.Role;
import com.moviebooking.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    // 1. Đổi tên từ getAllRoles thành findAll để khớp với Controller
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    // 2. Thêm phương thức save còn thiếu
    public Role save(Role role) {
        return roleRepository.save(role);
    }
}